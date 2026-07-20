package com.bulletin.dto.people;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String matricule;
    private String nom;
    private String postnom;
    private String prenom;
    private String sexe;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresse;
    private String telephoneParent;
    private String emailParent;
    private String photo;
    private String etat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
