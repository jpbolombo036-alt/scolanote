package com.bulletin.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Implémentation SMTP du fournisseur d'e-mail (via Spring JavaMailSender).
 *
 * Utilise la configuration spring.mail.* (host/port/username/password) déjà en place.
 * Les identifiants SMTP ne sont JAMAIS stockés dans le code (variables d'environnement).
 *
 * Ne lève JAMAIS d'exception : toute erreur est capturée et retournée dans
 * EmailSendResult.failure(), garantissant que la transaction métier n'est pas annulée.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    @Override
    public EmailSendResult sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(properties.getFrom(), properties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            return EmailSendResult.ok();
        } catch (Exception e) {
            // On journalise l'erreur ici (technique) ET on la retourne (pour le log BDD).
            log.warn("Échec d'envoi de l'e-mail à {} via {} : {}", to, getProviderName(), e.getMessage());
            return EmailSendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return properties.getProvider();
    }
}
