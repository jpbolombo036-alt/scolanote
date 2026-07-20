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
public class UserRoleResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long roleId;
    private String roleNom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
