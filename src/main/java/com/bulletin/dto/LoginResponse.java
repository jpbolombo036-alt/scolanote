package com.bulletin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse d'authentification professionnelle (login réussi).
 *
 * Contient le token JWT + sa durée de validité + les informations essentielles
 * de l'utilisateur (évite un second appel à /auth/me après le login).
 *
 * Suit les conventions OAuth2 (tokenType "Bearer", expiresIn en secondes).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** Le token JWT à envoyer dans le header "Authorization: Bearer <token>". */
    private String accessToken;

    /** Type du token (convention OAuth2 RFC 6750) : "Bearer". */
    private String tokenType;

    /** Durée de validité du token en secondes (ex: 86400 = 24h). */
    private Long expiresIn;

    /** Informations essentielles de l'utilisateur connecté. */
    private UserInfo user;

    /**
     * Informations essentielles d'un utilisateur authentifié.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private List<String> roles;
        private Long schoolId;
        private List<String> permissions;
    }
}
