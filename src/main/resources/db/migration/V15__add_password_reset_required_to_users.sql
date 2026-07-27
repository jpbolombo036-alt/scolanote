ALTER TABLE users
    ADD COLUMN password_reset_required boolean NOT NULL DEFAULT false;
