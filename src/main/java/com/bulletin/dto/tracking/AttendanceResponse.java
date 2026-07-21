package com.bulletin.dto.tracking;

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
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private Long periodId;
    private LocalDate date;
    private boolean retard;
    private boolean absence;
    private String motif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
