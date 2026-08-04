package com.bulletin.notification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration du module de notifications (externalisée via variables d'environnement).
 *
 * Préfixe : app.notification.mail.*
 *
 * Variables d'environnement correspondantes :
 *  - MAIL_PROVIDER      → provider (ex: smtp)
 *  - MAIL_FROM          → from (adresse d'expédition)
 *  - MAIL_FROM_NAME     → fromName (nom affiché de l'expéditeur)
 *  - FRONTEND_URL       → frontendUrl (liens dans les e-mails)
 *
 * Les identifiants SMTP (host/port/username/password) restent gérés par spring.mail.*
 * (déjà configurés) et ne sont JAMAIS stockés dans le code.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.notification.mail")
public class NotificationProperties {

    /** Fournisseur d'e-mail (ex: smtp). Permet de changer de fournisseur sans toucher au code. */
    private String provider = "smtp";

    /** Adresse d'expédition des e-mails. */
    private String from = "noreply@scolanote.com";

    /** Nom affiché de l'expéditeur (branding de l'application). */
    private String fromName = "ScolaNote";

    /** URL du frontend (pour les liens dans les e-mails : reset, consultation bulletin...). */
    private String frontendUrl = "http://localhost:3000";

    /** Nom de l'application (branding dans les templates). */
    private String appName = "ScolaNote";
}
