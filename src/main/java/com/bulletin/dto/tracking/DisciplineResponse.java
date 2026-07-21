package com.bulletin.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineResponse {

    private Long id;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private Long periodId;
    private String periodNom;
    private String conduite;
    private String application;
    private String observation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
