package com.bulletin.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Écoute les événements de notification et les traite de façon ASYNCHRONE.
 *
 * Utilise @TransactionalEventListener(AFTER_COMMIT) : l'événement n'est traité
 * qu'APRÈS le commit de la transaction métier, donc l'e-mail n'est jamais envoyé
 * si la transaction échoue (cohérence des données).
 *
 * @Async : l'envoi s'exécute dans un thread séparé (pool "notificationExecutor"),
 * la réponse HTTP n'est JAMAIS bloquée.
 *
 * Aucune exception n'est propagée (garantit de ne jamais annuler la transaction métier).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotification(NotificationEvent event) {
        try {
            notificationService.notify(event);
        } catch (Exception e) {
            // Ne jamais laisser remonter une exception depuis un listener asynchrone.
            log.error("Erreur dans le listener de notification {} : {}", event.getType(), e.getMessage(), e);
        }
    }
}
