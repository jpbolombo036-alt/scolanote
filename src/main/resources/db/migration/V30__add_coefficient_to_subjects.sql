-- Fix: l'entité Subject.java déclare un champ coefficient,
-- mais la table subjects (créée en V2) n'avait pas encore cette colonne.
-- Avec spring.jpa.hibernate.ddl-auto=validate, le démarrage échoue si elle manque.
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS coefficient INT;