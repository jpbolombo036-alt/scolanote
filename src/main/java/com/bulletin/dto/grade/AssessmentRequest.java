package com.bulletin.dto.grade;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRequest {

    @NotNull
    private Long assignmentId;

    @NotNull
    private Long assessmentTypeId;

    @NotNull
    private Long periodId;

    @Size(max = 200)
    private String titre;

    private LocalDate date;

    @NotNull
    private BigDecimal noteMax;

    private boolean publie;
}
