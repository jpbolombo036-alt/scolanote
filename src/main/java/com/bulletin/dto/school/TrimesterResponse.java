package com.bulletin.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrimesterResponse {
    private Long id;
    private Long academicYearId;
    private String academicYearLibelle;
    private String nom;
    private Integer ordre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
