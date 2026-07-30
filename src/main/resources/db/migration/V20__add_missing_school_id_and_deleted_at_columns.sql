-- Add missing school_id and deleted_at columns for entities that now require them

-- Common soft-delete marker columns
ALTER TABLE schools ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE levels ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE sections ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE options ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_templates ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE academic_years ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE students ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE curricula ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teaching_assignments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE assessment_types ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE assessments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE grades ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE disciplines ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_cards ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE trimesters ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE periods ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Missing multi-tenancy school_id columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_users_school_id ON users(school_id);

ALTER TABLE students ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_students_school_id ON students(school_id);

ALTER TABLE teachers ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_teachers_school_id ON teachers(school_id);

ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_classrooms_school_id ON classrooms(school_id);

ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_enrollments_school_id ON enrollments(school_id);

ALTER TABLE curricula ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_curricula_school_id ON curricula(school_id);

ALTER TABLE teaching_assignments ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_teaching_assignments_school_id ON teaching_assignments(school_id);

ALTER TABLE user_teachers ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_teachers ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_user_teachers_school_id ON user_teachers(school_id);

ALTER TABLE user_students ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_students ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_user_students_school_id ON user_students(school_id);

ALTER TABLE assessments ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_assessments_school_id ON assessments(school_id);

ALTER TABLE grades ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_grades_school_id ON grades(school_id);

ALTER TABLE attendances ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_attendances_school_id ON attendances(school_id);

ALTER TABLE disciplines ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_disciplines_school_id ON disciplines(school_id);

ALTER TABLE report_cards ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_report_cards_school_id ON report_cards(school_id);

ALTER TABLE assessment_types ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_assessment_types_school_id ON assessment_types(school_id);

ALTER TABLE levels ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_levels_school_id ON levels(school_id);

ALTER TABLE sections ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_sections_school_id ON sections(school_id);

ALTER TABLE options ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_options_school_id ON options(school_id);

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_subjects_school_id ON subjects(school_id);

ALTER TABLE periods ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_periods_school_id ON periods(school_id);

ALTER TABLE trimesters ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_trimesters_school_id ON trimesters(school_id);

ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS school_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_user_roles_school_id ON user_roles(school_id);
