-- Data migration: backfill school_id for existing records created before multi-tenancy
-- This script assigns orphan records to the first available school
-- Run this ONCE after deploying V17, before creating any new schools/data

DO $$
DECLARE
    default_school_id BIGINT;
BEGIN
    -- Find the first school to use as fallback
    SELECT id INTO default_school_id FROM schools ORDER BY id LIMIT 1;

    IF default_school_id IS NULL THEN
        RAISE NOTICE 'No school found. Create at least one school before running this migration.';
        RETURN;
    END IF;

    RAISE NOTICE 'Backfilling school_id = % for orphan records...', default_school_id;

    -- Subjects (matières)
    UPDATE subjects SET school_id = default_school_id WHERE school_id IS NULL;

    -- Levels (niveaux)
    UPDATE levels SET school_id = default_school_id WHERE school_id IS NULL;

    -- Sections
    UPDATE sections SET school_id = default_school_id WHERE school_id IS NULL;

    -- Options
    UPDATE options SET school_id = default_school_id WHERE school_id IS NULL;

    -- Trimesters
    UPDATE trimesters SET school_id = default_school_id WHERE school_id IS NULL;

    -- Periods
    UPDATE periods SET school_id = default_school_id WHERE school_id IS NULL;

    -- Assessment types
    UPDATE assessment_types SET school_id = default_school_id WHERE school_id IS NULL;

    -- User roles: follow the user's school_id
    UPDATE user_roles ur
    SET school_id = u.school_id
    FROM users u
    WHERE ur.user_id = u.id
      AND ur.school_id IS NULL
      AND u.school_id IS NOT NULL;

    -- User roles without a matching user school fallback to default
    UPDATE user_roles
    SET school_id = default_school_id
    WHERE school_id IS NULL;

    RAISE NOTICE 'Backfill complete.';
END $$;

-- Verify
SELECT 'subjects' AS table_name, COUNT(*) AS without_school FROM subjects WHERE school_id IS NULL
UNION ALL
SELECT 'levels', COUNT(*) FROM levels WHERE school_id IS NULL
UNION ALL
SELECT 'sections', COUNT(*) FROM sections WHERE school_id IS NULL
UNION ALL
SELECT 'options', COUNT(*) FROM options WHERE school_id IS NULL
UNION ALL
SELECT 'trimesters', COUNT(*) FROM trimesters WHERE school_id IS NULL
UNION ALL
SELECT 'periods', COUNT(*) FROM periods WHERE school_id IS NULL
UNION ALL
SELECT 'assessment_types', COUNT(*) FROM assessment_types WHERE school_id IS NULL
UNION ALL
SELECT 'user_roles', COUNT(*) FROM user_roles WHERE school_id IS NULL;
