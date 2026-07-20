-- Assessments, grades, attendance, discipline
CREATE TABLE IF NOT EXISTS assessment_types (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    coefficient INT,
    ordre INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT,
    assessment_type_id BIGINT,
    term_id BIGINT,
    titre VARCHAR(200),
    date DATE,
    note_max NUMERIC(8,2),
    publie BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_assessments_assignment FOREIGN KEY (assignment_id) REFERENCES teaching_assignments(id),
    CONSTRAINT fk_assessments_type FOREIGN KEY (assessment_type_id) REFERENCES assessment_types(id),
    CONSTRAINT fk_assessments_term FOREIGN KEY (term_id) REFERENCES terms(id)
);

CREATE TABLE IF NOT EXISTS grades (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT,
    student_id BIGINT,
    note NUMERIC(8,2),
    absence BOOLEAN DEFAULT FALSE,
    observation VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_grades_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id),
    CONSTRAINT fk_grades_student FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    date DATE,
    retard BOOLEAN DEFAULT FALSE,
    absence BOOLEAN DEFAULT FALSE,
    motif VARCHAR(300),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_attendances_student FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS disciplines (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    term_id BIGINT,
    conduite VARCHAR(30),
    application VARCHAR(30),
    observation VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_disciplines_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_disciplines_term FOREIGN KEY (term_id) REFERENCES terms(id)
);
