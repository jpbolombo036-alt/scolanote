package com.bulletin.dto.school;

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
public class SchoolRequest {

    @NotBlank
    @Size(max = 200)
    private String nom;

    @Size(max = 50)
    private String code;

    @Size(max = 300)
    private String adresse;

    @Size(max = 50)
    private String telephone;

    @Size(max = 150)
    private String email;

    @Size(max = 500)
    private String logo;
}
