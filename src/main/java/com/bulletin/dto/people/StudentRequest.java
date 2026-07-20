package com.bulletin.dto.people;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest {

    @Size(max = 50)
    private String matricule;

    @NotBlank
    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String postnom;

    @Size(max = 100)
    private String prenom;

    @Size(max = 10)
    private String sexe;

    private LocalDate dateNaissance;

    @Size(max = 150)
    private String lieuNaissance;

    @Size(max = 300)
    private String adresse;

    @Size(max = 50)
    private String telephoneParent;

    @Size(max = 150)
    private String emailParent;

    @Size(max = 500)
    private String photo;

    @Size(max = 30)
    private String etat;
}
