package com.bulletin.entity.enums;

import java.math.BigDecimal;

/**
 * Mentions du système scolaire congolais (RDC), par ordre décroissant de mérite.
 *
 * Une seule source de vérité pour la mention, utilisée à la génération du bulletin
 * (stockée en BDD) comme à l'affichage (PDF, dashboard, API) — sans double mapping.
 *
 * Les seuils par défaut correspondent à la config app.bulletin.mention.* :
 *   excellent   = 85
 *   tres-bien   = 70
 *   bien        = 60
 *   satisfaction= 50
 */
public enum Mention {
    EXCELLENT("Excellent"),
    TRES_BIEN("Très Bien"),
    BIEN("Bien"),
    PASSABLE("Passable"),
    INSUFFISANT("Insuffisant");

    private final String libelle;

    Mention(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Résout la mention à partir d'un pourcentage et des seuils (descendants).
     *
     * @param pourcentage le pourcentage global de l'élève (0-100)
     * @param seuilExcellent    seuil pour EXCELLENT (ex: 85)
     * @param seuilTresBien     seuil pour TRES_BIEN (ex: 70)
     * @param seuilBien         seuil pour BIEN (ex: 60)
     * @param seuilPassable     seuil pour PASSABLE (ex: 50)
     * @return la mention correspondante (jamais null)
     */
    public static Mention from(BigDecimal pourcentage,
                               BigDecimal seuilExcellent,
                               BigDecimal seuilTresBien,
                               BigDecimal seuilBien,
                               BigDecimal seuilPassable) {
        BigDecimal p = pourcentage != null ? pourcentage : BigDecimal.ZERO;
        if (p.compareTo(seuilExcellent) >= 0) return EXCELLENT;
        if (p.compareTo(seuilTresBien) >= 0) return TRES_BIEN;
        if (p.compareTo(seuilBien) >= 0) return BIEN;
        if (p.compareTo(seuilPassable) >= 0) return PASSABLE;
        return INSUFFISANT;
    }
}
