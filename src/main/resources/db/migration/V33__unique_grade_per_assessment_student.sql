-- V33: Unicité métier des notes — 1 élève = 1 note par évaluation
--
-- 1) Dédoublonnage DOUX des données existantes : les doublons (même couple
--    assessment_id/student_id) sont soft-deletés en conservant la note la plus
--    récemment mise à jour (tie-break : id le plus élevé = dernière saisie).
--    Aucune donnée n'est perdue (deleted_at seulement).
-- 2) Index unique PARTIEL (PostgreSQL) : seules les notes actives sont visées,
--    ce qui permet de ré-encoder une note après une suppression (soft-delete).
--
-- Comme V32, la syntaxe est PostgreSQL : Flyway est désactivé en local (H2),
-- l'index ne s'applique donc qu'en prod. En local, la protection est assurée
-- par le contrôle applicatif (GradeService.assertNoDuplicate).

UPDATE grades g
SET deleted_at = NOW()
WHERE g.deleted_at IS NULL
  AND g.assessment_id IS NOT NULL
  AND g.student_id IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM grades g2
      WHERE g2.assessment_id = g.assessment_id
        AND g2.student_id = g.student_id
        AND g2.deleted_at IS NULL
        AND (COALESCE(g2.updated_at, g2.created_at, '-infinity'::timestamp), g2.id)
          > (COALESCE(g.updated_at, g.created_at, '-infinity'::timestamp), g.id)
  );

DROP INDEX IF EXISTS uq_grades_assessment_student_active;
CREATE UNIQUE INDEX uq_grades_assessment_student_active
    ON grades(assessment_id, student_id)
    WHERE deleted_at IS NULL;
