-- Garantit la présence de deleted_at (et updated_at) sur TOUTES les tables métier.
-- Toutes les entités héritent désormais de BaseEntity (soft-delete global via
-- @SQLRestriction("deleted_at IS NULL")), donc chaque table mappée doit posséder
-- la colonne deleted_at pour passer la validation Hibernate (ddl-auto: validate).
-- Idempotent : ADD COLUMN IF NOT EXISTS n'échoue pas si la colonne existe déjà.

ALTER TABLE schools               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE academic_years        ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE trimesters            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE periods               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE levels                ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE sections              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE options               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE classrooms            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE subjects              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE curricula             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE curriculum_subjects   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teachers              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE students              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE enrollments           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teaching_assignments  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE assessment_types      ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE assessments           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE grades                ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE attendances           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE disciplines           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_templates      ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_cards          ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_card_details   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE users                 ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE roles                 ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_roles            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_teachers         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_students         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE auth_logs             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
