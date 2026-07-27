package com.bulletin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitAdminRequest {

    @NotBlank(message = "La clé d'initialisation est requise")
    private String initKey;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
    private String password;
}
