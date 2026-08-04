-- V24 : Journal des notifications (e-mails transactionnels).
-- Trace chaque envoi : date, destinataire, type, statut (SUCCESS/FAILED), erreur éventuelle.
-- Conforme au cahier des charges du module Notification.

CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(60) NOT NULL,            -- ex: USER_CREATED, BULLETIN_PUBLISHED
    channel VARCHAR(20) NOT NULL DEFAULT 'EMAIL',  -- EMAIL (futur: SMS, PUSH)
    recipient VARCHAR(255) NOT NULL,      -- adresse e-mail du destinataire
    subject VARCHAR(300),                 -- sujet de l'e-mail
    status VARCHAR(20) NOT NULL,          -- SUCCESS ou FAILED
    error_message VARCHAR(1000),          -- message d'erreur si FAILED
    reference_id BIGINT,                  -- id métier lié (ex: reportCardId, userId) optionnel
    school_id BIGINT,                     -- contexte multi-tenant (optionnel)
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_logs_recipient ON notification_logs(recipient);
CREATE INDEX IF NOT EXISTS idx_notification_logs_type ON notification_logs(type);
CREATE INDEX IF NOT EXISTS idx_notification_logs_status ON notification_logs(status);
CREATE INDEX IF NOT EXISTS idx_notification_logs_created_at ON notification_logs(created_at);
