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
public class LevelResponse {

    private Long id;
    private String nom;
    private Integer ordre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
