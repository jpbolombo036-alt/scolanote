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
public class CurriculumSubjectResponse {

    private Long id;
    private Long curriculumId;
    private String curriculumNom;
    private Long subjectId;
    private String subjectNom;
    private String subjectCode;
    private Integer coefficient;
    private Integer ordre;
    private boolean obligatoire;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
