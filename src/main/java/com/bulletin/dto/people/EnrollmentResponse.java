package com.bulletin.dto.people;

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
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private Long classroomId;
    private String classroomNom;
    private LocalDate dateInscription;
    private Integer numeroOrdre;
    private String etat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
