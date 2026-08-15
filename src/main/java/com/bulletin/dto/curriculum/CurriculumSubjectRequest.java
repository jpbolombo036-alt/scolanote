package com.bulletin.dto.curriculum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumSubjectRequest {

    @NotNull
    private Long curriculumId;

    @NotNull
    private Long subjectId;

    private Integer coefficient;

    /**
     * Ordre d'affichage de la matière dans le programme.
     * <b>Ignoré par le serveur</b> : calculé automatiquement (max(ordre)+1 dans le scope
     * programme + école). La valeur fournie par le client est ignorée en création et
     * immuable en modification.
     */
    private Integer ordre;

    private boolean obligatoire;
}
