package com.bulletin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@bulletin.local}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        log.info("Tentative d'envoi d'email de réinitialisation à: {}", toEmail);

        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetHtml(username, resetLink);

            sendHtmlEmail(toEmail, "Réinitialisation de votre mot de passe - Bulletin Gestion", htmlContent);

            log.info("Email de réinitialisation envoyé avec succès à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email de réinitialisation", e);
        }
    }

    public void sendBulletinNotificationEmail(String toEmail, String studentName, String periodLabel) {
        log.info("Tentative d'envoi de notification de bulletin à: {}", toEmail);

        try {
            String htmlContent = buildBulletinNotificationHtml(studentName, periodLabel);
            sendHtmlEmail(toEmail, "Nouveau bulletin disponible - Bulletin Gestion", htmlContent);
            log.info("Notification de bulletin envoyée avec succès à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email de notification", e);
        }
    }

    private String buildPasswordResetHtml(String username, String resetLink) {
        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif\">"
                + "<h2>Réinitialisation de votre mot de passe</h2>"
                + "<p>Bonjour " + username + ",</p>"
                + "<p>Une demande de réinitialisation de mot de passe a été effectuée. "
                + "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe :</p>"
                + "<p><a href=\"" + resetLink + "\">Réinitialiser mon mot de passe</a></p>"
                + "<p>Ce lien expire dans 30 minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>"
                + "</body></html>";
    }

    private String buildBulletinNotificationHtml(String studentName, String periodLabel) {
        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif\">"
                + "<h2>Nouveau bulletin disponible</h2>"
                + "<p>Le bulletin de " + studentName + " pour " + periodLabel + " est désormais disponible.</p>"
                + "<p>Connectez-vous à votre espace pour le consulter.</p>"
                + "</body></html>";
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (jakarta.mail.MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email HTML", e);
        }
    }
}
