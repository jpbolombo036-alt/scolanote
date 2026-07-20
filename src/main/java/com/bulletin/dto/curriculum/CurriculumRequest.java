package com.bulletin.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
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
public class CurriculumRequest {

    private Long levelId;
    private Long sectionId;
    private Long optionId;

    @NotBlank
    @Size(max = 150)
    private String nom;
}
