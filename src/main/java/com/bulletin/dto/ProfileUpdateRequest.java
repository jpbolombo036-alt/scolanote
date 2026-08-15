package com.bulletin.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @Email(message = "L'email doit être valide")
    private String email;

    private String telephone;

    private String nom;

    private String postnom;

    private String prenom;
}
