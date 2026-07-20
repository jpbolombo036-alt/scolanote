package com.bulletin.dto.curriculum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumSubjectRequest {

    @NotNull
    private Long curriculumId;

    @NotNull
    private Long subjectId;

    private Integer coefficient;

    private Integer ordre;

    private boolean obligatoire;
}
