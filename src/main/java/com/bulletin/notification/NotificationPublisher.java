package com.bulletin.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Point d'entrée unique pour publier des événements de notification depuis les services métier.
 *
 * Centralise la publication (bonne pratique) : les services métier n'ont pas besoin de connaître
 * ApplicationEventPublisher — ils appellent simplement notificationPublisher.publish(event).
 *
 * La publication est synchrone et non-bloquante : l'événement est ensuite traité de façon
 * asynchrone par NotificationListener (après commit de la transaction).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publie un événement de notification.
     * La méthode est non-bloquante et ne lève jamais d'exception vers l'appelant.
     */
    public void publish(NotificationEvent event) {
        if (event == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(event);
            log.debug("Événement de notification publié: {} -> {}", event.getType(), event.getRecipient());
        } catch (Exception e) {
            // Une erreur de publication ne doit JAMAIS impacter la transaction métier.
            log.warn("Impossible de publier l'événement de notification {} : {}", event.getType(), e.getMessage());
        }
    }

    /** Raccourci : publie un événement construit à la volée. */
    public void publish(NotificationType type, String recipient, NotificationEvent.Builder builder) {
        if (builder != null) {
            publish(builder.build());
        }
    }
}
