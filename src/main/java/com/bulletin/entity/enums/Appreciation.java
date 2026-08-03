package com.bulletin.entity.enums;

/**
 * Appréciation qualitative (conduite et application) du système scolaire congolais.
 *
 * Utilisée pour les champs `conduite` et `application` (Discipline, ReportCard).
 * Échelle standard RDC, de la meilleure à la plus faible :
 *   TB  = Très Bien
 *   B   = Bien
 *   AB  = Assez Bien
 *   P   = Passable
 *   M   = Mauvais
 *
 * Le champ reste un String en BDD (compatibilité avec les données existantes) ;
 * cet enum fournit les codes/libellés canoniques et des helpers de validation.
 */
public enum Appreciation {
    TB("TB", "Très Bien"),
    B("B", "Bien"),
    AB("AB", "Assez Bien"),
    P("P", "Passable"),
    M("M", "Mauvais");

    private final String code;
    private final String libelle;

    Appreciation(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Indique si la valeur fournie est une appréciation valide (code ou libellé). */
    public static boolean isValid(String value) {
        return from(value) != null;
    }

    /**
     * Résout l'appréciation à partir d'un code (TB/B/AB/P/M) ou d'un libellé
     * (insensible à la casse et aux accents). Retourne null si inconnu.
     */
    public static Appreciation from(String value) {
        if (value == null) return null;
        String v = normalize(value);
        for (Appreciation a : values()) {
            if (normalize(a.code).equals(v)
                    || normalize(a.libelle).equals(v)
                    || normalize(a.name()).equals(v)) {
                return a;
            }
        }
        return null;
    }

    /** Retourne le libellé complet d'une valeur (code ou libellé), ou la valeur d'origine si inconnue. */
    public static String toLibelle(String value) {
        Appreciation a = from(value);
        return a != null ? a.getLibelle() : value;
    }

    private static String normalize(String s) {
        return s.trim()
                .toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace(" ", "");
    }
}
