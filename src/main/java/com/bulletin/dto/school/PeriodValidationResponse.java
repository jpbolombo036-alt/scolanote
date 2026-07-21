package com.bulletin.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodValidationResponse {
    private boolean peutVerrouiller;
    private int nombreEvaluations;
    private long nombreNotes;
    private long notesManquantes;
    private String message;
}
