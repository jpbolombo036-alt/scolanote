package com.bulletin.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAgentRequest {

    @Size(min = 3, max = 50, message = "Le username doit contenir entre 3 et 50 caractères")
    private String username;

    @Size(min = 3, max = 100, message = "L'email doit contenir entre 3 et 100 caractères")
    private String email;

    @Size(min = 8, max = 20, message = "Le téléphone doit contenir entre 8 et 20 caractères")
    private String telephone;

    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String password;

    @Size(min = 3, max = 30, message = "Le rôle doit contenir entre 3 et 30 caractères")
    private String role;
}
