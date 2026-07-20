package com.bulletin.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTypeResponse {

    private Long id;
    private String nom;
    private Integer coefficient;
    private Integer ordre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
