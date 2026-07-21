-- Add province and commune_territoire to schools table
ALTER TABLE schools ADD COLUMN IF NOT EXISTS province VARCHAR(100);
ALTER TABLE schools ADD COLUMN IF NOT EXISTS commune_territoire VARCHAR(100);
