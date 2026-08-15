package com.bulletin.dto.people;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long classroomId;

    private LocalDate dateInscription;

    /**
     * Numéro d'ordre de l'élève dans la classe.
     * <b>Ignoré par le serveur</b> : calculé automatiquement (max(numero_ordre)+1 dans le
     * scope classe + école). La valeur fournie par le client est ignorée en création et
     * immuable en modification.
     */
    private Integer numeroOrdre;

    @Size(max = 30)
    private String etat;
}
