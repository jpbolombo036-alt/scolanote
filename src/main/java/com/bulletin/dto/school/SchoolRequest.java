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

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String province;

    @NotBlank
    @Size(max = 100)
    private String communeTerritoire;

    @Size(max = 300)
    private String adresse;

    @Size(max = 50)
    private String telephone;

    @NotBlank
    @Size(max = 150)
    private String email;

    @Size(max = 500)
    private String logo;
}
