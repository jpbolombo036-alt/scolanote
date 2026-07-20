package com.bulletin.dto.grade;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotNull
    private Long assessmentId;

    @NotNull
    private Long studentId;

    private BigDecimal note;

    private boolean absence;

    @Size(max = 500)
    private String observation;
}
