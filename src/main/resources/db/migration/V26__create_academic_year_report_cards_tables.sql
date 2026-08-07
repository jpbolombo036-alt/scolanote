-- V26 : Création des tables pour les bulletins annuels (academic_year_report_cards).

CREATE TABLE IF NOT EXISTS academic_year_report_cards (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    academic_year_id BIGINT NOT NULL,
    pourcentage NUMERIC(5,2),
    total_points NUMERIC(10,2),
    maximum_points NUMERIC(10,2),
    rang INT,
    mention VARCHAR(50),
    decision VARCHAR(50),
    total_absences INT,
    total_retards INT,
    conduite VARCHAR(30),
    application VARCHAR(30),
    date_generation TIMESTAMP,
    pdf_url VARCHAR(500),
    statut VARCHAR(30),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_ayrc_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    CONSTRAINT fk_ayrc_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id)
);

CREATE TABLE IF NOT EXISTS academic_year_report_card_details (
    id BIGSERIAL PRIMARY KEY,
    academic_year_report_card_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    coefficient INT,
    moyenne NUMERIC(5,2),
    points NUMERIC(10,2),
    maximum NUMERIC(10,2),
    pourcentage NUMERIC(5,2),
    rang_matiere INT,
    observation VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_ayrc_details_card FOREIGN KEY (academic_year_report_card_id) REFERENCES academic_year_report_cards(id),
    CONSTRAINT fk_ayrc_details_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE INDEX IF NOT EXISTS idx_ayrc_enrollment_id ON academic_year_report_cards(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_ayrc_academic_year_id ON academic_year_report_cards(academic_year_id);
CREATE INDEX IF NOT EXISTS idx_ayrc_details_card_id ON academic_year_report_card_details(academic_year_report_card_id);
