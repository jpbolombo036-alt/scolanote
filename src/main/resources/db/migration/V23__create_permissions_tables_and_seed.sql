-- ============================================================
-- V23 : Système de permissions granulaires (RBAC)
-- Crée les tables permissions + role_permissions et les seede
-- en reproduisant le comportement actuel des 5 rôles.
-- Idempotent : INSERT ... ON CONFLICT DO NOTHING / IF NOT EXISTS.
-- ============================================================

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    categorie VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id),
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- ============================================================
-- Catalogue des permissions (code, libelle, categorie)
-- ============================================================
INSERT INTO permissions (code, libelle, categorie, created_at, updated_at) VALUES
-- Écoles (réservé SUPER_ADMIN)
('ECOLE_VOIR', 'Voir les écoles', 'ECOLES', NOW(), NOW()),
('ECOLE_CREER', 'Créer une école', 'ECOLES', NOW(), NOW()),
('ECOLE_MODIFIER', 'Modifier une école', 'ECOLES', NOW(), NOW()),
('ECOLE_SUPPRIMER', 'Supprimer une école', 'ECOLES', NOW(), NOW()),
-- Années scolaires / périodes
('ANNEE_GERER', 'Gérer les années scolaires', 'STRUCTURE', NOW(), NOW()),
('TRIMESTRE_GERER', 'Gérer les trimestres', 'STRUCTURE', NOW(), NOW()),
('PERIODE_GERER', 'Gérer les périodes', 'STRUCTURE', NOW(), NOW()),
('PERIODE_VERROUILLER', 'Verrouiller/déverrouiller une période', 'STRUCTURE', NOW(), NOW()),
-- Structure pédagogique
('CLASSE_GERER', 'Gérer les classes', 'STRUCTURE', NOW(), NOW()),
('MATIERE_GERER', 'Gérer les matières', 'STRUCTURE', NOW(), NOW()),
('CURRICULUM_GERER', 'Gérer les programmes', 'STRUCTURE', NOW(), NOW()),
-- Personnes
('ELEVE_GERER', 'Gérer les élèves', 'PERSONNES', NOW(), NOW()),
('ENSEIGNANT_GERER', 'Gérer les enseignants', 'PERSONNES', NOW(), NOW()),
('INSCRIPTION_GERER', 'Gérer les inscriptions', 'PERSONNES', NOW(), NOW()),
('AFFECTATION_GERER', 'Gérer les affectations d''enseignement', 'PERSONNES', NOW(), NOW()),
-- Évaluations et notes
('EVALUATION_VOIR', 'Voir les évaluations', 'NOTES', NOW(), NOW()),
('EVALUATION_CREER', 'Créer une évaluation', 'NOTES', NOW(), NOW()),
('NOTE_SAISIR', 'Saisir les notes', 'NOTES', NOW(), NOW()),
('NOTE_MODIFIER', 'Modifier les notes', 'NOTES', NOW(), NOW()),
-- Suivi (présences / discipline)
('PRESENCE_GERER', 'Gérer les présences', 'SUIVI', NOW(), NOW()),
('DISCIPLINE_GERER', 'Gérer la discipline', 'SUIVI', NOW(), NOW()),
-- Bulletins
('BULLETIN_VOIR', 'Voir les bulletins', 'BULLETINS', NOW(), NOW()),
('BULLETIN_GENERER', 'Générer les bulletins', 'BULLETINS', NOW(), NOW()),
('BULLETIN_VALIDER_PREFET', 'Valider (préfet)', 'BULLETINS', NOW(), NOW()),
('BULLETIN_VALIDER_DIRECTEUR', 'Valider (directeur)', 'BULLETINS', NOW(), NOW()),
('BULLETIN_SIGNER', 'Signer un bulletin', 'BULLETINS', NOW(), NOW()),
('BULLETIN_PUBLIER', 'Publier un bulletin', 'BULLETINS', NOW(), NOW()),
-- Administration
('UTILISATEUR_GERER', 'Gérer les utilisateurs', 'ADMIN', NOW(), NOW()),
('ROLE_GERER', 'Gérer les rôles et permissions', 'ADMIN', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Association rôles <-> permissions (reproduit le comportement actuel)
-- ============================================================

-- SUPER_ADMIN : TOUTES les permissions
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.nom = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ADMIN : tout sauf la gestion des écoles (ECOLE_*)
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.nom = 'ADMIN'
  AND p.code NOT LIKE 'ECOLE_%'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- DIRECTEUR : gestion pédagogique, bulletins (tout le workflow), suivi, périodes
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.nom = 'DIRECTEUR'
  AND p.code IN (
    'ANNEE_GERER','TRIMESTRE_GERER','PERIODE_GERER','PERIODE_VERROUILLER',
    'CLASSE_GERER','MATIERE_GERER','CURRICULUM_GERER',
    'ELEVE_GERER','ENSEIGNANT_GERER','INSCRIPTION_GERER','AFFECTATION_GERER',
    'EVALUATION_VOIR','PRESENCE_GERER','DISCIPLINE_GERER',
    'BULLETIN_VOIR','BULLETIN_GENERER','BULLETIN_VALIDER_PREFET','BULLETIN_VALIDER_DIRECTEUR',
    'BULLETIN_SIGNER','BULLETIN_PUBLIER'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- PREFET : suivi disciplinaire + validation préfet des bulletins + périodes
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.nom = 'PREFET'
  AND p.code IN (
    'PRESENCE_GERER','DISCIPLINE_GERER',
    'BULLETIN_VOIR','BULLETIN_VALIDER_PREFET','PERIODE_VERROUILLER'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ENSEIGNANT : évaluations et notes (ses affectations) + consultation bulletins
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.nom = 'ENSEIGNANT'
  AND p.code IN (
    'EVALUATION_VOIR','EVALUATION_CREER','NOTE_SAISIR','NOTE_MODIFIER','BULLETIN_VOIR'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;
