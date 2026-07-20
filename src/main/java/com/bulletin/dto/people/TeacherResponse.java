package com.bulletin.dto.people;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private Long id;
    private String matricule;
    private String nom;
    private String postnom;
    private String prenom;
    private String telephone;
    private String email;
    private String specialite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
