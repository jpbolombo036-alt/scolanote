-- V32: Auto-ordre serveur + contraintes d'unicité partielles
--
-- Objectif : garantir, au niveau de la base, l'unicité de l'ordre/numero_ordre
-- serveur-calculé (max + 1) dans chaque scope. La contrainte est PARTIELLE
-- (WHERE ... IS NOT NULL) : les enregistrements existants dont l'ordre est NULL
-- ne sont pas touchés (décision du plan — pas de backfill d'ordre).
--
-- Ces index sont PostgreSQL (syntaxe de clause WHERE). Flyway est désactivé en local
-- (H2), les index ne s'appliquent donc qu'en prod — conformément au plan.

-- curriculum_subjects manquait de school_id : la contrainte d'unicité du plan
-- porte sur (curriculum_id, school_id, ordre). On ajoute la colonne et on la
-- retro-patie depuis le programme (level/section/option -> school_id).
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

-- =========================================================
-- Déduplication avant création des index uniques
-- Stratégie : supprimer les lignes "filles" (id plus grand) en gardant
-- la ligne "mère" (id le plus petit) pour chaque (scope, ordre).
-- Les soft-supprimées (deleted_at IS NOT NULL) sont supprimées en priorité.
-- =========================================================

-- a) curriculum_subjects : (curriculum_id, school_id, ordre)
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

-- b) enrollments : (classroom_id, school_id, numero_ordre)
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

-- c) levels : (school_id, ordre)
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

-- d) trimesters : (academic_year_id, school_id, ordre)
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

-- e) periods : (trimester_id, school_id, ordre)
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

-- 1) levels : unicité (school_id, ordre)
DROP INDEX IF EXISTS uq_levels_school_ordre;
CREATE UNIQUE INDEX uq_levels_school_ordre
    ON levels(school_id, ordre)
    WHERE ordre IS NOT NULL;

-- 2) trimesters : unicité (academic_year_id, school_id, ordre)
DROP INDEX IF EXISTS uq_trimesters_academic_year_school_ordre;
CREATE UNIQUE INDEX uq_trimesters_academic_year_school_ordre
    ON trimesters(academic_year_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- 3) periods : unicité (trimester_id, school_id, ordre)
DROP INDEX IF EXISTS uq_periods_trimester_school_ordre;
CREATE UNIQUE INDEX uq_periods_trimester_school_ordre
    ON periods(trimester_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- 4) curriculum_subjects : unicité (curriculum_id, school_id, ordre)
DROP INDEX IF EXISTS uq_curriculum_subjects_curriculum_school_ordre;
CREATE UNIQUE INDEX uq_curriculum_subjects_curriculum_school_ordre
    ON curriculum_subjects(curriculum_id, school_id, ordre)
    WHERE ordre IS NOT NULL;

-- 5) enrollments : unicité (classroom_id, school_id, numero_ordre)
DROP INDEX IF EXISTS uq_enrollments_classroom_school_numero_ordre;
CREATE UNIQUE INDEX uq_enrollments_classroom_school_numero_ordre
    ON enrollments(classroom_id, school_id, numero_ordre)
    WHERE numero_ordre IS NOT NULL;
