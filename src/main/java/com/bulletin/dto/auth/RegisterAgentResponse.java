package com.bulletin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAgentResponse {
    private Long id;
    private String username;
    private String email;
    private String telephone;
    private String role;
    private String message;
}
