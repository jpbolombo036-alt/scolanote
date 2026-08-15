package com.bulletin.exception;

/**
 * Levée lorsqu'une création/modification violerait une règle d'unicité métier
 * (ex : une note existe déjà pour le couple évaluation-élève).
 * Mappée en HTTP 409 CONFLICT par {@link GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
