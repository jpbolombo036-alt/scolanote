package com.bulletin.dto.tracking;

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
public class AttendanceRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long periodId;

    @NotNull
    private LocalDate date;

    private boolean retard;
    private boolean absence;

    @Size(max = 300)
    private String motif;
}
