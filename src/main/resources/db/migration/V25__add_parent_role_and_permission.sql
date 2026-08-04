-- V25 : Ajoute le rôle PARENT et lui attribue la permission BULLETIN_VOIR.
-- Le parent peut consulter les bulletins de son enfant.
-- Idempotent : INSERT ... ON CONFLICT DO NOTHING / IF NOT EXISTS.

-- 1. Le rôle PARENT (au cas où il n'existerait pas encore en base)
INSERT INTO roles (nom, created_at, updated_at)
SELECT 'PARENT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'PARENT');

-- 2. Attribuer BULLETIN_VOIR au rôle PARENT
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r, permissions p
WHERE r.nom = 'PARENT'
  AND p.code = 'BULLETIN_VOIR'
ON CONFLICT (role_id, permission_id) DO NOTHING;
