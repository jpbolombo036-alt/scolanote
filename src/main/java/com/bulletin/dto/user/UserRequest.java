package com.bulletin.dto.user;

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
public class UserRequest {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    private boolean enabled;

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String postnom;

    @Size(max = 100)
    private String prenom;

    private java.util.List<String> roles;
}
