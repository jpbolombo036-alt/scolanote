package com.bulletin.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service d'orchestration des notifications.
 *
 * Responsabilité unique : à partir d'un NotificationEvent,
 *  1. rendre le template (EmailTemplateService),
 *  2. envoyer via le fournisseur (EmailProvider),
 *  3. journaliser le résultat en base (NotificationLog).
 *
 * Ne lève JAMAIS d'exception : toute erreur est capturée et journalisée,
 * garantissant que la transaction métier appelante n'est JAMAIS annulée.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailTemplateService templateService;
    private final EmailProvider emailProvider;
    private final NotificationLogRepository logRepository;

    /**
     * Traite une notification : rend le template, envoie l'e-mail, journalise.
     * Méthode totalement sûre (aucune exception propagée).
     */
    public void notify(NotificationEvent event) {
        String subject = event.getType().getDefaultSubject();
        try {
            // 1. Rendre le template HTML
            String htmlBody = templateService.render(event.getType(), event.getVariables());

            // 2. Envoyer via le fournisseur (ne lève jamais d'exception)
            EmailProvider.EmailSendResult result = emailProvider.sendHtmlEmail(
                    event.getRecipient(), subject, htmlBody);

            // 3. Journaliser le résultat en base
            saveLog(event, subject, result);

            if (result.success()) {
                log.info("Notification {} envoyée à {}", event.getType(), event.getRecipient());
            }
        } catch (Exception e) {
            // Sécurité ultime : même une erreur inattendue (template, BDD...) ne doit pas remonter.
            log.error("Erreur inattendue lors de la notification {} à {} : {}",
                    event.getType(), event.getRecipient(), e.getMessage(), e);
            saveLogFailure(event, subject, e.getMessage());
        }
    }

    private void saveLog(NotificationEvent event, String subject, EmailProvider.EmailSendResult result) {
        try {
            logRepository.save(NotificationLog.builder()
                    .type(event.getType())
                    .channel(NotificationLog.Channel.EMAIL)
                    .recipient(event.getRecipient())
                    .subject(subject)
                    .status(result.success() ? NotificationLog.Status.SUCCESS : NotificationLog.Status.FAILED)
                    .errorMessage(result.errorMessage())
                    .referenceId(event.getReferenceId())
                    .schoolId(event.getSchoolId())
                    .build());
        } catch (Exception e) {
            log.error("Impossible de journaliser la notification {} : {}", event.getType(), e.getMessage());
        }
    }

    private void saveLogFailure(NotificationEvent event, String subject, String errorMessage) {
        try {
            logRepository.save(NotificationLog.builder()
                    .type(event.getType())
                    .channel(NotificationLog.Channel.EMAIL)
                    .recipient(event.getRecipient())
                    .subject(subject)
                    .status(NotificationLog.Status.FAILED)
                    .errorMessage(errorMessage)
                    .referenceId(event.getReferenceId())
                    .schoolId(event.getSchoolId())
                    .build());
        } catch (Exception e) {
            log.error("Impossible de journaliser l'échec de notification {} : {}", event.getType(), e.getMessage());
        }
    }
}
