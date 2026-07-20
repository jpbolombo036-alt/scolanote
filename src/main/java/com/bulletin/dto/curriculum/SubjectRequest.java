package com.bulletin.dto.curriculum;

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
public class SubjectRequest {

    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String nom;

    @Size(max = 500)
    private String description;
}
