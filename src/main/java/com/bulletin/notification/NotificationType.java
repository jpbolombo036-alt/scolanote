package com.bulletin.notification;

/**
 * Types de notifications transactionnelles (centralisation des constantes).
 *
 * Chaque type correspond à un événement métier qui déclenche un e-mail,
 * et à un template Thymeleaf associé (voir EmailTemplateService).
 *
 * Extensible (Open/Closed) : ajouter un type = ajouter une entrée + un template,
 * sans modifier le code existant.
 */
public enum NotificationType {
    /** Un utilisateur est créé → e-mail de bienvenue. */
    USER_CREATED("welcome", "Bienvenue sur ScolaNote"),

    /** Un utilisateur active son compte → confirmation d'activation. */
    USER_ACTIVATED("account-activated", "Votre compte est activé"),

    /** Demande de réinitialisation de mot de passe (par l'utilisateur). */
    PASSWORD_RESET("password-reset", "Réinitialisation de votre mot de passe"),

    /** Réinitialisation du mot de passe par un administrateur. */
    PASSWORD_RESET_BY_ADMIN("password-reset", "Votre mot de passe a été réinitialisé"),

    /** Le mot de passe a été modifié → confirmation de sécurité. */
    PASSWORD_CHANGED("password-changed", "Votre mot de passe a été modifié"),

    /** Connexion détectée depuis un nouvel appareil (optionnel). */
    NEW_DEVICE_LOGIN("new-device", "Nouvelle connexion à votre compte"),

    /** Un bulletin est publié → notification aux parents/utilisateur. */
    BULLETIN_PUBLISHED("bulletin-published", "Nouveau bulletin disponible"),

    /** Une facture ou un paiement est validé → confirmation. */
    PAYMENT_VALIDATED("payment-confirmed", "Confirmation de paiement"),

    /** Un compte est désactivé → notification. */
    ACCOUNT_DISABLED("account-disabled", "Votre compte a été désactivé"),

    /** Vérification d'adresse e-mail (futur). */
    VERIFY_EMAIL("verify-email", "Vérifiez votre adresse e-mail");

    /** Nom du template Thymeleaf (dans templates/mail/, sans extension). */
    private final String template;

    /** Sujet par défaut de l'e-mail. */
    private final String defaultSubject;

    NotificationType(String template, String defaultSubject) {
        this.template = template;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplate() {
        return template;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }
}
