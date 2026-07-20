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
public class AcademicYearRequest {

    @NotNull
    private Long schoolId;

    @NotBlank
    private String libelle;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean active;
}
