package com.bulletin.dto.people;

import jakarta.validation.constraints.Email;
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
public class TeacherRequest {

    @Size(max = 50)
    private String matricule;

    @NotBlank
    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String postnom;

    @Size(max = 100)
    private String prenom;

    @Size(max = 50)
    private String telephone;

    /**
     * Email du professeur — OBLIGATOIRE : il devient le username du compte utilisateur
     * créé automatiquement (rôle ENSEIGNANT). Un e-mail de bienvenue y est envoyé.
     */
    @NotBlank(message = "L'email du professeur est obligatoire (il servira d'identifiant de connexion)")
    @Email(message = "L'email doit être valide")
    @Size(max = 150)
    private String email;

    @Size(max = 100)
    private String specialite;
}
