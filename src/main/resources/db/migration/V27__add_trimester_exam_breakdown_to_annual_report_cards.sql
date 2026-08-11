-- V27 : Ajout des colonnes de detail par trimestre et examen pour les bulletins annuels.

ALTER TABLE academic_year_report_card_details
    ADD COLUMN IF NOT EXISTS moyenne_t1 NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS moyenne_t2 NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS moyenne_t3 NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS moyenne_examen NUMERIC(5,2);
