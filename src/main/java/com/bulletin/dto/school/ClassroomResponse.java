package com.bulletin.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {

    private Long id;
    private Long academicYearId;
    private String academicYearLibelle;
    private Long levelId;
    private String levelNom;
    private Long sectionId;
    private String sectionNom;
    private Long optionId;
    private String optionNom;
    private Long reportTemplateId;
    private String reportTemplateNom;
    private String nom;
    private Integer capacite;
    private Long titulaireId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
