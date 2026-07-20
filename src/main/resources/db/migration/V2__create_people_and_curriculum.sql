-- Students, teachers, subjects, curricula and classrooms
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    matricule VARCHAR(50),
    nom VARCHAR(100) NOT NULL,
    postnom VARCHAR(100),
    prenom VARCHAR(100),
    sexe VARCHAR(10),
    date_naissance DATE,
    lieu_naissance VARCHAR(150),
    adresse VARCHAR(300),
    telephone_parent VARCHAR(50),
    email_parent VARCHAR(150),
    photo VARCHAR(500),
    etat VARCHAR(30),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS teachers (
    id BIGSERIAL PRIMARY KEY,
    matricule VARCHAR(50),
    nom VARCHAR(100) NOT NULL,
    postnom VARCHAR(100),
    prenom VARCHAR(100),
    telephone VARCHAR(50),
    email VARCHAR(150),
    specialite VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS classrooms (
    id BIGSERIAL PRIMARY KEY,
    academic_year_id BIGINT,
    level_id BIGINT,
    section_id BIGINT,
    option_id BIGINT,
    report_template_id BIGINT,
    nom VARCHAR(100) NOT NULL,
    capacite INT,
    titulaire_id BIGINT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_classrooms_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_classrooms_level FOREIGN KEY (level_id) REFERENCES levels(id),
    CONSTRAINT fk_classrooms_section FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT fk_classrooms_option FOREIGN KEY (option_id) REFERENCES options(id),
    CONSTRAINT fk_classrooms_template FOREIGN KEY (report_template_id) REFERENCES report_templates(id)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    classroom_id BIGINT,
    date_inscription DATE,
    numero_ordre INT,
    etat VARCHAR(30),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_enrollments_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20),
    nom VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS curricula (
    id BIGSERIAL PRIMARY KEY,
    level_id BIGINT,
    section_id BIGINT,
    option_id BIGINT,
    nom VARCHAR(150) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_curricula_level FOREIGN KEY (level_id) REFERENCES levels(id),
    CONSTRAINT fk_curricula_section FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT fk_curricula_option FOREIGN KEY (option_id) REFERENCES options(id)
);

CREATE TABLE IF NOT EXISTS curriculum_subjects (
    id BIGSERIAL PRIMARY KEY,
    curriculum_id BIGINT,
    subject_id BIGINT,
    coefficient INT,
    ordre INT,
    obligatoire BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_curriculum_subjects_curriculum FOREIGN KEY (curriculum_id) REFERENCES curricula(id),
    CONSTRAINT fk_curriculum_subjects_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE IF NOT EXISTS teaching_assignments (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT,
    classroom_id BIGINT,
    subject_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_teaching_assignments_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    CONSTRAINT fk_teaching_assignments_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    CONSTRAINT fk_teaching_assignments_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE IF NOT EXISTS user_teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    teacher_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_teachers_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_teachers_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

CREATE TABLE IF NOT EXISTS user_students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    student_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_students_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_students_student FOREIGN KEY (student_id) REFERENCES students(id)
);
