-- Ajoute la colonne deleted_at aux tables qui ne l'avaient pas encore.
-- Requis car toutes les entités héritent désormais de BaseEntity (soft-delete global
-- via @SQLRestriction("deleted_at IS NULL")), ce qui impose la colonne deleted_at
-- sur toutes les tables mappées, pour la validation Hibernate (ddl-auto: validate).

ALTER TABLE auth_logs ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE report_card_details ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- updated_at manquait aussi à auth_logs (BaseEntity le fournit désormais)
ALTER TABLE auth_logs ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
