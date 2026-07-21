-- Create trimesters table
CREATE TABLE IF NOT EXISTS trimesters (
    id BIGSERIAL PRIMARY KEY,
    academic_year_id BIGINT,
    nom VARCHAR(100) NOT NULL,
    ordre INT,
    date_debut DATE,
    date_fin DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_trimesters_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id)
);

CREATE INDEX IF NOT EXISTS idx_trimesters_academic_year ON trimesters(academic_year_id);

-- Create periods table
CREATE TABLE IF NOT EXISTS periods (
    id BIGSERIAL PRIMARY KEY,
    trimester_id BIGINT,
    nom VARCHAR(100) NOT NULL,
    ordre INT,
    type VARCHAR(20),
    date_debut DATE,
    date_fin DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_periods_trimester FOREIGN KEY (trimester_id) REFERENCES trimesters(id)
);

CREATE INDEX IF NOT EXISTS idx_periods_trimester ON periods(trimester_id);
