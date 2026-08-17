-- V32: Auto-ordre serveur + contraintes d'unicité partielles
--
-- Objectif : garantir l'unicité de l'ordre/numero_ordre serveur-calculé
-- (max + 1) dans chaque scope. La contrainte est PARTIELLE (WHERE ... IS NOT NULL).
--
-- Ces index sont PostgreSQL (syntaxe WHERE). Flyway est désactivé en local (H2),
-- les index ne s'appliquent donc qu'en prod — conformément au plan.

-- Disable timeout to prevent migration killing on large data
SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = 0;

-- =====================================================================
-- 0) Ajout de la colonne school_id sur curriculum_subjects + backfill
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 0: Adding school_id to curriculum_subjects'; END $$;

ALTER TABLE curriculum_subjects ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_curriculum_subjects_school_id ON curriculum_subjects(school_id);

UPDATE curriculum_subjects cs
SET school_id = src.school_id
FROM (
    SELECT cs2.id AS id,
           COALESCE(l.school_id, s.school_id, o.school_id) AS school_id
    FROM curriculum_subjects cs2
    JOIN curricula c        ON c.id = cs2.curriculum_id
    LEFT JOIN levels l      ON l.id = c.level_id
    LEFT JOIN sections s    ON s.id = c.section_id
    LEFT JOIN options o     ON o.id = c.option_id
) src
WHERE cs.id = src.id
  AND cs.school_id IS NULL;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 0: Done (school_id added and backfilled)'; END $$;

-- =====================================================================
-- 1a) curriculum_subjects : deduplicate (curriculum_id, school_id, ordre)
--     No FK references TO curriculum_subjects.
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1a: Deduplicating curriculum_subjects'; END $$;

DROP TABLE IF EXISTS _v32_dup_cs;
CREATE TABLE _v32_dup_cs AS
    SELECT curriculum_id, school_id, ordre, MIN(id) AS min_id
    FROM curriculum_subjects
    WHERE ordre IS NOT NULL
    GROUP BY curriculum_id, school_id, ordre
    HAVING COUNT(*) > 1;

DELETE FROM curriculum_subjects child
USING _v32_dup_cs dup
WHERE child.curriculum_id = dup.curriculum_id
  AND child.school_id = dup.school_id
  AND child.ordre = dup.ordre
  AND child.id > dup.min_id;

DROP TABLE IF EXISTS _v32_dup_cs;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1a: Done (curriculum_subjects deduplicated)'; END $$;

-- =====================================================================
-- 1b) enrollments : deduplicate (classroom_id, school_id, numero_ordre)
--     FK refs: report_cards.enrollment_id, academic_year_report_cards.enrollment_id
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1b: Reassigning enrollment FKs and deduplicating'; END $$;

-- Build the dup->keep mapping for enrollments
DROP TABLE IF EXISTS _v32_dup_enr;
CREATE TABLE _v32_dup_enr AS
    SELECT child.id AS dup_id, dup.min_id AS keep_id
    FROM enrollments child
    JOIN (
        SELECT classroom_id, school_id, numero_ordre, MIN(id) AS min_id
        FROM enrollments
        WHERE numero_ordre IS NOT NULL
        GROUP BY classroom_id, school_id, numero_ordre
        HAVING COUNT(*) > 1
    ) dup ON child.classroom_id = dup.classroom_id
       AND child.school_id = dup.school_id
       AND child.numero_ordre = dup.numero_ordre
       AND child.id > dup.min_id;

-- Reassign FKs in dependent tables BEFORE deleting duplicates
UPDATE report_cards rc
SET enrollment_id = de.keep_id
FROM _v32_dup_enr de
WHERE rc.enrollment_id = de.dup_id;

UPDATE academic_year_report_cards acr
SET enrollment_id = de.keep_id
FROM _v32_dup_enr de
WHERE acr.enrollment_id = de.dup_id;

-- Now safe to delete duplicates
DELETE FROM enrollments child
USING _v32_dup_enr de
WHERE child.id = de.dup_id;

DROP TABLE IF EXISTS _v32_dup_enr;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1b: Done (enrollments deduplicated)'; END $$;

-- =====================================================================
-- 1c) levels : deduplicate (school_id, ordre)
--     FK refs: classrooms.level_id, curricula.level_id
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1c: Reassigning level FKs and deduplicating'; END $$;

DROP TABLE IF EXISTS _v32_dup_lev;
CREATE TABLE _v32_dup_lev AS
    SELECT child.id AS dup_id, dup.min_id AS keep_id
    FROM levels child
    JOIN (
        SELECT school_id, ordre, MIN(id) AS min_id
        FROM levels
        WHERE ordre IS NOT NULL
        GROUP BY school_id, ordre
        HAVING COUNT(*) > 1
    ) dup ON child.school_id = dup.school_id
       AND child.ordre = dup.ordre
       AND child.id > dup.min_id;

UPDATE classrooms c
SET level_id = dl.keep_id
FROM _v32_dup_lev dl
WHERE c.level_id = dl.dup_id;

UPDATE curricula c
SET level_id = dl.keep_id
FROM _v32_dup_lev dl
WHERE c.level_id = dl.dup_id;

DELETE FROM levels child
USING _v32_dup_lev dl
WHERE child.id = dl.dup_id;

DROP TABLE IF EXISTS _v32_dup_lev;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1c: Done (levels deduplicated)'; END $$;

-- =====================================================================
-- 1d) trimesters : deduplicate (academic_year_id, school_id, ordre)
--     FK refs: periods.trimester_id
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1d: Reassigning trimester FKs and deduplicating'; END $$;

DROP TABLE IF EXISTS _v32_dup_trim;
CREATE TABLE _v32_dup_trim AS
    SELECT child.id AS dup_id, dup.min_id AS keep_id
    FROM trimesters child
    JOIN (
        SELECT academic_year_id, school_id, ordre, MIN(id) AS min_id
        FROM trimesters
        WHERE ordre IS NOT NULL
        GROUP BY academic_year_id, school_id, ordre
        HAVING COUNT(*) > 1
    ) dup ON child.academic_year_id = dup.academic_year_id
       AND child.school_id = dup.school_id
       AND child.ordre = dup.ordre
       AND child.id > dup.min_id;

UPDATE periods p
SET trimester_id = dt.keep_id
FROM _v32_dup_trim dt
WHERE p.trimester_id = dt.dup_id;

DELETE FROM trimesters child
USING _v32_dup_trim dt
WHERE child.id = dt.dup_id;

DROP TABLE IF EXISTS _v32_dup_trim;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1d: Done (trimesters deduplicated)'; END $$;

-- =====================================================================
-- 1e) periods : deduplicate (trimester_id, school_id, ordre)
--     FK refs: assessments.period_id, attendances.period_id,
--              disciplines.period_id, report_cards.period_id
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1e: Reassigning period FKs and deduplicating'; END $$;

DROP TABLE IF EXISTS _v32_dup_per;
CREATE TABLE _v32_dup_per AS
    SELECT child.id AS dup_id, dup.min_id AS keep_id
    FROM periods child
    JOIN (
        SELECT trimester_id, school_id, ordre, MIN(id) AS min_id
        FROM periods
        WHERE ordre IS NOT NULL
        GROUP BY trimester_id, school_id, ordre
        HAVING COUNT(*) > 1
    ) dup ON child.trimester_id = dup.trimester_id
       AND child.school_id = dup.school_id
       AND child.ordre = dup.ordre
       AND child.id > dup.min_id;

UPDATE assessments a
SET period_id = dp.keep_id
FROM _v32_dup_per dp
WHERE a.period_id = dp.dup_id;

UPDATE attendances a
SET period_id = dp.keep_id
FROM _v32_dup_per dp
WHERE a.period_id = dp.dup_id;

UPDATE disciplines d
SET period_id = dp.keep_id
FROM _v32_dup_per dp
WHERE d.period_id = dp.dup_id;

UPDATE report_cards rc
SET period_id = dp.keep_id
FROM _v32_dup_per dp
WHERE rc.period_id = dp.dup_id;

DELETE FROM periods child
USING _v32_dup_per dp
WHERE child.id = dp.dup_id;

DROP TABLE IF EXISTS _v32_dup_per;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 1e: Done (periods deduplicated)'; END $$;

-- =====================================================================
-- 2) Index uniques partiels (après déduplication)
-- =====================================================================

DO $$ BEGIN RAISE NOTICE 'V32 STEP 2: Creating partial unique indexes'; END $$;

-- levels : unicité (school_id, ordre)
DROP INDEX IF EXISTS uq_levels_school_ordre;
CREATE UNIQUE INDEX uq_levels_school_ordre
    ON levels(school_id, ordre)
    WHERE ordre IS NOT NULL;

-- trimesters : unicité (academic_year_id, school_id, ordre)
DROP INDEX IF EXISTS uq_trimesters_academic_year_school_ordre;
CREATE UNIQUE INDEX uq_trimesters_academic_year_school_ordre
    ON trimesters(academic_year_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- periods : unicité (trimester_id, school_id, ordre)
DROP INDEX IF EXISTS uq_periods_trimester_school_ordre;
CREATE UNIQUE INDEX uq_periods_trimester_school_ordre
    ON periods(trimester_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- curriculum_subjects : unicité (curriculum_id, school_id, ordre)
DROP INDEX IF EXISTS uq_curriculum_subjects_curriculum_school_ordre;
CREATE UNIQUE INDEX uq_curriculum_subjects_curriculum_school_ordre
    ON curriculum_subjects(curriculum_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- enrollments : unicité (classroom_id, school_id, numero_ordre)
DROP INDEX IF EXISTS uq_enrollments_classroom_school_numero_ordre;
CREATE UNIQUE INDEX uq_enrollments_classroom_school_numero_ordre
    ON enrollments(classroom_id, school_id, numero_ordre)
    WHERE numero_ordre IS NOT NULL;

DO $$ BEGIN RAISE NOTICE 'V32 STEP 2: Done (all indexes created). V32 COMPLETE.'; END $$;
