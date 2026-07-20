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
public class TeachingAssignmentResponse {

    private Long id;
    private Long teacherId;
    private String teacherNom;
    private Long classroomId;
    private String classroomNom;
    private Long subjectId;
    private String subjectNom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
