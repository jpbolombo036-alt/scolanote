package com.bulletin.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrimesterRequest {
    @NotNull
    private Long academicYearId;

    @NotBlank
    private String nom;

    /**
     * Ordre d'affichage du trimestre.
     * <b>Ignoré par le serveur</b> : calculé automatiquement (max(ordre)+1 dans le scope
     * année scolaire + école). La valeur fournie par le client est ignorée en création
     * et immuable en modification.
     */
    private Integer ordre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
