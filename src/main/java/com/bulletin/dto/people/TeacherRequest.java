package com.bulletin.dto.people;

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

    @Size(max = 150)
    private String email;

    @Size(max = 100)
    private String specialite;
}
