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
public class TeachingAssignmentRequest {

    @NotNull
    private Long teacherId;

    @NotNull
    private Long classroomId;

    @NotNull
    private Long subjectId;
}
