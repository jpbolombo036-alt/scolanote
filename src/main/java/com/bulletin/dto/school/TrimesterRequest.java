package com.bulletin.dto.school;

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
public class TrimesterRequest {
    @NotNull
    private Long academicYearId;

    @NotBlank
    private String nom;

    private Integer ordre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
