-- Add period_id columns to tables that reference terms
ALTER TABLE assessments ADD COLUMN IF NOT EXISTS period_id BIGINT;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS period_id BIGINT;
ALTER TABLE disciplines ADD COLUMN IF NOT EXISTS period_id BIGINT;
ALTER TABLE report_cards ADD COLUMN IF NOT EXISTS period_id BIGINT;

-- Migrate term_id to period_id
UPDATE assessments a SET period_id = p.id
FROM terms t
JOIN periods p ON p.nom = t.nom AND p.trimester_id IN (SELECT id FROM trimesters WHERE academic_year_id = t.academic_year_id)
WHERE a.term_id = t.id;

UPDATE attendances a SET period_id = p.id
FROM terms t
JOIN periods p ON p.nom = t.nom AND p.trimester_id IN (SELECT id FROM trimesters WHERE academic_year_id = t.academic_year_id)
WHERE a.term_id = t.id;

UPDATE disciplines d SET period_id = p.id
FROM terms t
JOIN periods p ON p.nom = t.nom AND p.trimester_id IN (SELECT id FROM trimesters WHERE academic_year_id = t.academic_year_id)
WHERE d.term_id = t.id;

UPDATE report_cards rc SET period_id = p.id
FROM terms t
JOIN periods p ON p.nom = t.nom AND p.trimester_id IN (SELECT id FROM trimesters WHERE academic_year_id = t.academic_year_id)
WHERE rc.term_id = t.id;
