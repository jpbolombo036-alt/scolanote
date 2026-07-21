package com.bulletin.dto.school;

import com.bulletin.entity.Period;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodRequest {
    @NotNull
    private Long trimesterId;

    @NotBlank
    private String nom;

    private Integer ordre;
    private Period.PeriodType type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
