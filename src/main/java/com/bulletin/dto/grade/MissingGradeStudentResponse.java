package com.bulletin.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Élève n'ayant pas encore de note pour une évaluation donnée.
 * Utilisé par la saisie en grille : au fur et à mesure de l'encodage,
 * les élèves disparaissent de cette liste.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingGradeStudentResponse {

    private Long studentId;
    private String matricule;
    private String nom;
    private String postnom;
    private String prenom;

    /** Numéro d'ordre de l'élève dans la classe (ordre d'appel). */
    private Integer numeroOrdre;
}
