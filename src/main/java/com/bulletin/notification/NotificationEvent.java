package com.bulletin.notification;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Événement de notification (immutable).
 *
 * Publié par les services métier via ApplicationEventPublisher, puis écouté
 * par NotificationListener (asynchrone). Ne contient JAMAIS d'informations sensibles
 * (mots de passe, tokens en clair, etc.).
 *
 * Pattern DTO + Open/Closed : les "variables" alimentent le template Thymeleaf.
 */
@Getter
public class NotificationEvent {

    /** Type de notification (détermine le template et le sujet). */
    private final NotificationType type;

    /** Adresse e-mail du destinataire. */
    private final String recipient;

    /** Nom d'affichage du destinataire (optionnel, pour personnaliser le template). */
    private final String recipientName;

    /** Variables du template (nom, prénom, école, lien, code, date, année scolaire...). */
    private final Map<String, Object> variables;

    /** Identifiant métier lié (ex: userId, reportCardId) — pour la traçabilité (optionnel). */
    private final Long referenceId;

    /** Contexte multi-tenant (optionnel). */
    private final Long schoolId;

    private NotificationEvent(Builder builder) {
        this.type = builder.type;
        this.recipient = builder.recipient;
        this.recipientName = builder.recipientName;
        this.variables = Collections.unmodifiableMap(new HashMap<>(builder.variables));
        this.referenceId = builder.referenceId;
        this.schoolId = builder.schoolId;
    }

    public static Builder builder(NotificationType type, String recipient) {
        return new Builder(type, recipient);
    }

    public static class Builder {
        private final NotificationType type;
        private final String recipient;
        private String recipientName;
        private final Map<String, Object> variables = new HashMap<>();
        private Long referenceId;
        private Long schoolId;

        private Builder(NotificationType type, String recipient) {
            this.type = type;
            this.recipient = recipient;
        }

        public Builder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        /** Ajoute une variable de template. */
        public Builder variable(String key, Object value) {
            if (key != null && value != null) {
                this.variables.put(key, value);
            }
            return this;
        }

        /** Ajoute plusieurs variables de template. */
        public Builder variables(Map<String, Object> vars) {
            if (vars != null) {
                vars.forEach(this::variable);
            }
            return this;
        }

        public Builder referenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder schoolId(Long schoolId) {
            this.schoolId = schoolId;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(this);
        }
    }
}
