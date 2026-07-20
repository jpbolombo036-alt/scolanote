-- Report cards (bulletin), details and auth logs
CREATE TABLE IF NOT EXISTS report_cards (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT,
    term_id BIGINT,
    pourcentage NUMERIC(8,2),
    total_points NUMERIC(10,2),
    maximum_points NUMERIC(10,2),
    rang INT,
    mention VARCHAR(50),
    decision VARCHAR(50),
    date_generation TIMESTAMP,
    pdf_url VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_report_cards_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    CONSTRAINT fk_report_cards_term FOREIGN KEY (term_id) REFERENCES terms(id)
);

CREATE TABLE IF NOT EXISTS report_card_details (
    id BIGSERIAL PRIMARY KEY,
    report_card_id BIGINT,
    subject_id BIGINT,
    coefficient INT,
    moyenne NUMERIC(8,2),
    points NUMERIC(10,2),
    maximum NUMERIC(10,2),
    pourcentage NUMERIC(8,2),
    observation VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_report_card_details_card FOREIGN KEY (report_card_id) REFERENCES report_cards(id),
    CONSTRAINT fk_report_card_details_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE IF NOT EXISTS auth_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100),
    success BOOLEAN NOT NULL,
    error_reason VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auth_logs_username ON auth_logs(username);
CREATE INDEX IF NOT EXISTS idx_auth_logs_created_at ON auth_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_auth_logs_success ON auth_logs(success);
