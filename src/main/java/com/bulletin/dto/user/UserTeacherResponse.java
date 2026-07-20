package com.bulletin.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTeacherResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long teacherId;
    private String teacherNom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
