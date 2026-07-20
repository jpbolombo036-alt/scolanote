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
public class AcademicYearResponse {

    private Long id;
    private Long schoolId;
    private String schoolNom;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
