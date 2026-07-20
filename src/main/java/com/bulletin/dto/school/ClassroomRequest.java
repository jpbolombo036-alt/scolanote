package com.bulletin.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomRequest {

    @NotNull
    private Long academicYearId;

    @NotNull
    private Long levelId;

    @NotNull
    private Long sectionId;

    private Long optionId;

    private Long reportTemplateId;

    @NotBlank
    private String nom;

    private Integer capacite;
    private Long titulaireId;
    private boolean active;
}
