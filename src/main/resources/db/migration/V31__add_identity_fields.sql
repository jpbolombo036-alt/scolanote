-- Identité des parents / utilisateurs :
--  - users : le compte (parent, enseignant, agent...) mérite un vrai nom/prénom,
--    pas seulement un username (actuellement l'email du parent).
--  - students : on mémorise le nom complet du parent/tuteur de l'élève,
--    utilisé pour provisionner le compte PARENT avec son identité.
ALTER TABLE users ADD COLUMN IF NOT EXISTS nom VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS postnom VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS prenom VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS nom_parent VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS postnom_parent VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS prenom_parent VARCHAR(100);