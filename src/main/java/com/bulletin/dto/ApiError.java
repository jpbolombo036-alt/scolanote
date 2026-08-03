package com.bulletin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Erreur API professionnelle : un code stable (machine-lisible) + un message humain.
 *
 * Le "code" permet au frontend de réagir de façon fiable (switch sur le code)
 * sans dépendre du texte du message (qui peut changer ou être traduit).
 *
 * Exemples de codes : INVALID_CREDENTIALS, ACCOUNT_DISABLED, PASSWORD_RESET_REQUIRED,
 *                     VALIDATION_ERROR, FORBIDDEN, NOT_FOUND, SERVER_ERROR.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /** Code d'erreur stable, machine-lisible (ex: INVALID_CREDENTIALS). */
    private String code;

    /** Message d'erreur lisible par l'utilisateur (en français). */
    private String message;
}
