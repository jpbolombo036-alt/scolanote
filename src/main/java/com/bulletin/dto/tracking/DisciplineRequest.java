package com.bulletin.dto.tracking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long termId;

    @Size(max = 30)
    private String conduite;

    @Size(max = 30)
    private String application;

    @Size(max = 500)
    private String observation;
}
