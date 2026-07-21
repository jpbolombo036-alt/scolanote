-- Migrate existing terms data to trimesters and periods
-- First, create trimesters from the unique academic_year + trimestre combinations
INSERT INTO trimesters (academic_year_id, nom, ordre, date_debut, date_fin, created_at, updated_at)
SELECT DISTINCT
    t.academic_year_id,
    CASE t.trimestre
        WHEN 1 THEN '1er Trimestre'
        WHEN 2 THEN '2e Trimestre'
        WHEN 3 THEN '3e Trimestre'
        ELSE 'Trimestre ' || t.trimestre
    END,
    t.trimestre,
    MIN(t.date_debut),
    MAX(t.date_fin),
    NOW(),
    NOW()
FROM terms t
GROUP BY t.academic_year_id, t.trimestre;

-- Then, migrate each term as a period
INSERT INTO periods (trimester_id, nom, ordre, type, date_debut, date_fin, created_at, updated_at)
SELECT
    tr.id,
    t.nom,
    t.ordre,
    CASE
        WHEN t.nom ILIKE '%examen%' THEN 'EXAMEN'
        ELSE 'PERIODE'
    END,
    t.date_debut,
    t.date_fin,
    NOW(),
    NOW()
FROM terms t
JOIN trimesters tr ON tr.academic_year_id = t.academic_year_id
    AND tr.ordre = t.trimestre;
