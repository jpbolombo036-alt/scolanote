package com.bulletin.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelRequest {

    @NotBlank
    private String nom;

    /**
     * Ordre d'affichage du niveau.
     * <b>Ignoré par le serveur</b> à la création et en modification : l'ordre est
     * calculé automatiquement serveur (max(ordre)+1 dans l'école) et la valeur fournie
     * par le client n'est jamais prise en compte.
     */
    private Integer ordre;
}
