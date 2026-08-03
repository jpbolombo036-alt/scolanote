package com.bulletin.entity.enums;

/**
 * État d'une personne (élève, enseignant) ou d'une inscription.
 *
 * Utilisé comme référence pour la cohérence et la validation des champs `etat`.
 * Le champ reste un String en BDD (compatibilité avec les données existantes) ;
 * cet enum fournit les valeurs canoniques et des helpers de validation.
 */
public enum EtatPersonne {
    ACTIF("Actif"),
    INACTIF("Inactif"),
    SUSPENDU("Suspendu"),
    DIPLOME("Diplômé"),
    TRANSFERE("Transféré"),
    ABANDON("Abandon");

    private final String libelle;

    EtatPersonne(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Indique si la valeur fournie (insensible à la casse/espaces) est un état valide. */
    public static boolean isValid(String value) {
        return from(value) != null;
    }

    /**
     * Résout l'état à partir d'une chaîne (accepte le nom de l'enum ou le libellé,
     * insensible à la casse et aux accents courants). Retourne null si inconnu.
     */
    public static EtatPersonne from(String value) {
        if (value == null) return null;
        String v = normalize(value);
        for (EtatPersonne e : values()) {
            if (normalize(e.name()).equals(v) || normalize(e.libelle).equals(v)) {
                return e;
            }
        }
        return null;
    }

    /** Valeur par défaut à la création d'une personne. */
    public static String defaultValue() {
        return ACTIF.getLibelle();
    }

    private static String normalize(String s) {
        return s.trim()
                .toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace(" ", "_");
    }
}
