package com.bulletin.dto.school;

import com.bulletin.entity.Period;
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
public class PeriodRequest {
    @NotNull
    private Long trimesterId;

    @NotBlank
    private String nom;

    /**
     * Ordre d'affichage de la période.
     * <b>Ignoré par le serveur</b> : calculé automatiquement (max(ordre)+1 dans le scope
     * trimestre + école). La valeur fournie par le client est ignorée en création et
     * immuable en modification.
     */
    private Integer ordre;
    private Period.PeriodType type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
