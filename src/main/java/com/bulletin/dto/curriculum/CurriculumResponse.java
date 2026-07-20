package com.bulletin.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumResponse {

    private Long id;
    private Long levelId;
    private String levelNom;
    private Long sectionId;
    private String sectionNom;
    private Long optionId;
    private String optionNom;
    private String nom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
