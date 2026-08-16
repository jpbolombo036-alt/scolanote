-- V32: Auto-ordre serveur + contraintes d'unicité partielles
--
-- Objectif : garantir, au niveau de la base, l'unicité de l'ordre/numero_ordre
-- serveur-calculé (max + 1) dans chaque scope. La contrainte est PARTIELLE
-- (WHERE ... IS NOT NULL) : les enregistrements existants dont l'ordre est NULL
-- ne sont pas touchés (décision du plan — pas de backfill d'ordre).
--
-- Ces index sont PostgreSQL (syntaxe de clause WHERE). Flyway est désactivé en local
-- (H2), les index ne s'appliquent donc qu'en prod — conformément au plan.

-- =====================================================================
-- 0) Ajout de la colonne school_id sur curriculum_subjects + backfill
--    La colonne school_id n'existait pas encore sur curriculum_subjects
--    (aucune migration précédente ne l'ajoute). On la retro-patie depuis
--    la hiérarchie curriculum → level/section/option → school_id.
-- =====================================================================

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

-- =====================================================================
-- 1) Déduplication avec réassignment des clés étrangères
--    Avant de supprimer les lignes "doublons" (id > MIN(id)), on
--    réassigne les références FK des tables dépendantes vers la ligne
--    conservée (MIN(id)). Cela évite les violations de contrainte FK
--    sur les lignes existantes qui référencent les doublons.
-- =====================================================================

-- ---------------------------------------------------------------------
-- a) curriculum_subjects : (curriculum_id, school_id, ordre)
--    Aucune table ne possède de FK vers curriculum_subjects.
--    Suppression directe des doublons.
-- ---------------------------------------------------------------------

DELETE FROM curriculum_subjects child
USING (
    SELECT curriculum_id, school_id, ordre, MIN(id) AS min_id
    FROM curriculum_subjects
    WHERE ordre IS NOT NULL
    GROUP BY curriculum_id, school_id, ordre
    HAVING COUNT(*) > 1
) dup
WHERE child.curriculum_id = dup.curriculum_id
  AND child.school_id = dup.school_id
  AND child.ordre = dup.ordre
  AND child.id > dup.min_id;

-- ---------------------------------------------------------------------
-- b) enrollments : (classroom_id, school_id, numero_ordre)
--    FK référençant enrollments :
--      - report_cards.enrollment_id
--      - academic_year_report_cards.enrollment_id
--    On réassigne ces FK vers la ligne conservée avant de supprimer.
-- ---------------------------------------------------------------------

WITH dup_enrollments AS (
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
       AND child.id > dup.min_id
)
UPDATE report_cards rc
SET enrollment_id = de.keep_id
FROM dup_enrollments de
WHERE rc.enrollment_id = de.dup_id;

WITH dup_enrollments AS (
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
       AND child.id > dup.min_id
)
UPDATE academic_year_report_cards acr
SET enrollment_id = de.keep_id
FROM dup_enrollments de
WHERE acr.enrollment_id = de.dup_id;

DELETE FROM enrollments child
USING (
    SELECT classroom_id, school_id, numero_ordre, MIN(id) AS min_id
    FROM enrollments
    WHERE numero_ordre IS NOT NULL
    GROUP BY classroom_id, school_id, numero_ordre
    HAVING COUNT(*) > 1
) dup
WHERE child.classroom_id = dup.classroom_id
  AND child.school_id = dup.school_id
  AND child.numero_ordre = dup.numero_ordre
  AND child.id > dup.min_id;

-- ---------------------------------------------------------------------
-- c) levels : (school_id, ordre)
--    FK référençant levels :
--      - classrooms.level_id
--      - curricula.level_id
--    On réassigne ces FK vers la ligne conservée avant de supprimer.
-- ---------------------------------------------------------------------

WITH dup_levels AS (
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
       AND child.id > dup.min_id
)
UPDATE classrooms c
SET level_id = dl.keep_id
FROM dup_levels dl
WHERE c.level_id = dl.dup_id;

WITH dup_levels AS (
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
       AND child.id > dup.min_id
)
UPDATE curricula c
SET level_id = dl.keep_id
FROM dup_levels dl
WHERE c.level_id = dl.dup_id;

DELETE FROM levels child
USING (
    SELECT school_id, ordre, MIN(id) AS min_id
    FROM levels
    WHERE ordre IS NOT NULL
    GROUP BY school_id, ordre
    HAVING COUNT(*) > 1
) dup
WHERE child.school_id = dup.school_id
  AND child.ordre = dup.ordre
  AND child.id > dup.min_id;

-- ---------------------------------------------------------------------
-- d) trimesters : (academic_year_id, school_id, ordre)
--    FK référençant trimesters :
--      - periods.trimester_id
--    On réassigne cette FK vers la ligne conservée avant de supprimer.
-- ---------------------------------------------------------------------

WITH dup_trimesters AS (
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
       AND child.id > dup.min_id
)
UPDATE periods p
SET trimester_id = dt.keep_id
FROM dup_trimesters dt
WHERE p.trimester_id = dt.dup_id;

DELETE FROM trimesters child
USING (
    SELECT academic_year_id, school_id, ordre, MIN(id) AS min_id
    FROM trimesters
    WHERE ordre IS NOT NULL
    GROUP BY academic_year_id, school_id, ordre
    HAVING COUNT(*) > 1
) dup
WHERE child.academic_year_id = dup.academic_year_id
  AND child.school_id = dup.school_id
  AND child.ordre = dup.ordre
  AND child.id > dup.min_id;

-- ---------------------------------------------------------------------
-- e) periods : (trimester_id, school_id, ordre)
--    FK référençant periods :
--      - assessments.period_id
--      - attendances.period_id
--      - disciplines.period_id
--      - report_cards.period_id
--    On réassigne ces FK vers la ligne conservée avant de supprimer.
-- ---------------------------------------------------------------------

WITH dup_periods AS (
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
       AND child.id > dup.min_id
)
UPDATE assessments a
SET period_id = dp.keep_id
FROM dup_periods dp
WHERE a.period_id = dp.dup_id;

UPDATE attendances a
SET period_id = dp.keep_id
FROM dup_periods dp
WHERE a.period_id = dp.dup_id;

UPDATE disciplines d
SET period_id = dp.keep_id
FROM dup_periods dp
WHERE d.period_id = dp.dup_id;

UPDATE report_cards rc
SET period_id = dp.keep_id
FROM dup_periods dp
WHERE rc.period_id = dp.dup_id;

DELETE FROM periods child
USING (
    SELECT trimester_id, school_id, ordre, MIN(id) AS min_id
    FROM periods
    WHERE ordre IS NOT NULL
    GROUP BY trimester_id, school_id, ordre
    HAVING COUNT(*) > 1
) dup
WHERE child.trimester_id = dup.trimester_id
  AND child.school_id = dup.school_id
  AND child.ordre = dup.ordre
  AND child.id > dup.min_id;

-- =====================================================================
-- 2) Index uniques partiels (après déduplication)
-- =====================================================================

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
