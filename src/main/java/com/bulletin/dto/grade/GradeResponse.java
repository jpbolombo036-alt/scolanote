package com.bulletin.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {

    private Long id;
    private Long assessmentId;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private BigDecimal note;
    private boolean absence;
    private String observation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
