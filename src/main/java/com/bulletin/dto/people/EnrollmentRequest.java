package com.bulletin.dto.people;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long classroomId;

    private LocalDate dateInscription;

    private Integer numeroOrdre;

    @Size(max = 30)
    private String etat;
}
