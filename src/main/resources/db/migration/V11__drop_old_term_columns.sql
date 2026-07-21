-- Drop old foreign key constraints
ALTER TABLE assessments DROP CONSTRAINT IF EXISTS fk_assessments_term;
ALTER TABLE disciplines DROP CONSTRAINT IF EXISTS fk_disciplines_term;
ALTER TABLE report_cards DROP CONSTRAINT IF EXISTS fk_report_cards_term;

-- Drop old term_id columns
ALTER TABLE assessments DROP COLUMN IF EXISTS term_id;
ALTER TABLE attendances DROP COLUMN IF EXISTS term_id;
ALTER TABLE disciplines DROP COLUMN IF EXISTS term_id;
ALTER TABLE report_cards DROP COLUMN IF EXISTS term_id;

-- Add foreign key constraints for period_id
ALTER TABLE assessments ADD CONSTRAINT fk_assessments_period FOREIGN KEY (period_id) REFERENCES periods(id);
ALTER TABLE attendances ADD CONSTRAINT fk_attendances_period FOREIGN KEY (period_id) REFERENCES periods(id);
ALTER TABLE disciplines ADD CONSTRAINT fk_disciplines_period FOREIGN KEY (period_id) REFERENCES periods(id);
ALTER TABLE report_cards ADD CONSTRAINT fk_report_cards_period FOREIGN KEY (period_id) REFERENCES periods(id);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_assessments_period ON assessments(period_id);
CREATE INDEX IF NOT EXISTS idx_attendances_period ON attendances(period_id);
CREATE INDEX IF NOT EXISTS idx_disciplines_period ON disciplines(period_id);
CREATE INDEX IF NOT EXISTS idx_report_cards_period ON report_cards(period_id);

-- Drop terms table (optional, keep for now in case rollback needed)
-- DROP TABLE IF EXISTS terms;
