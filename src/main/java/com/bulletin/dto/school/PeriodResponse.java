package com.bulletin.dto.school;

import com.bulletin.entity.Period;
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
public class PeriodResponse {
    private Long id;
    private Long trimesterId;
    private String trimesterNom;
    private String nom;
    private Integer ordre;
    private Period.PeriodType type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean verrouille;
    private LocalDateTime dateVerrouillage;
    private String verrouillePar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
