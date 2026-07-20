package com.bulletin.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponse {

    private Long id;
    private String nom;
    private String code;
    private String adresse;
    private String telephone;
    private String email;
    private String logo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
