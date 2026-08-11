-- V28 : Ajout de school_id sur academic_year_report_cards et index.

ALTER TABLE academic_year_report_cards
    ADD COLUMN IF NOT EXISTS school_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_ayrc_school_id ON academic_year_report_cards(school_id);