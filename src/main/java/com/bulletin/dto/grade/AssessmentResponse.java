package com.bulletin.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;
    private Long assignmentId;
    private Long assessmentTypeId;
    private String assessmentTypeNom;
    private Long periodId;
    private String periodNom;
    private String titre;
    private LocalDate date;
    private BigDecimal noteMax;
    private boolean publie;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
