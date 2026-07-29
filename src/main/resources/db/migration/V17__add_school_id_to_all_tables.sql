-- Add school_id to tables that represent school-specific configuration/data

-- Subjects (matières)
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE subjects ADD CONSTRAINT fk_subjects_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_subjects_school_id ON subjects(school_id);

-- Levels (niveaux)
ALTER TABLE levels ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE levels ADD CONSTRAINT fk_levels_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_levels_school_id ON levels(school_id);

-- Sections
ALTER TABLE sections ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE sections ADD CONSTRAINT fk_sections_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_sections_school_id ON sections(school_id);

-- Options
ALTER TABLE options ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE options ADD CONSTRAINT fk_options_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_options_school_id ON options(school_id);

-- Trimesters
ALTER TABLE trimesters ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE trimesters ADD CONSTRAINT fk_trimesters_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_trimesters_school_id ON trimesters(school_id);

-- Periods
ALTER TABLE periods ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE periods ADD CONSTRAINT fk_periods_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_periods_school_id ON periods(school_id);

-- Assessment types
ALTER TABLE assessment_types ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE assessment_types ADD CONSTRAINT fk_assessment_types_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_assessment_types_school_id ON assessment_types(school_id);

-- User roles
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS school_id BIGINT;
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_school_id FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_user_roles_school_id ON user_roles(school_id);
