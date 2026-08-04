package com.bulletin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

/**
 * Service de rendu des templates d'e-mail (Thymeleaf).
 *
 * Rend les templates HTML situés dans templates/mail/ en y injectant les variables
 * (nom, prénom, école, lien, code, date, année scolaire...) + le branding de l'application.
 *
 * Sépare la présentation (HTML) de la logique métier — bonne pratique.
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final SpringTemplateEngine templateEngine;
    private final NotificationProperties properties;

    /**
     * Rend un template d'e-mail en HTML.
     *
     * @param type      type de notification (détermine le template)
     * @param variables variables injectées dans le template
     * @return le contenu HTML rendu
     */
    public String render(NotificationType type, Map<String, Object> variables) {
        Context context = new Context(Locale.FRENCH);

        // Branding commun à tous les templates (disponible comme variables appName, frontendUrl, etc.)
        context.setVariable("appName", properties.getAppName());
        context.setVariable("frontendUrl", properties.getFrontendUrl());

        // Variables métier de l'événement
        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        // Le template est dans templates/mail/<template>.html
        return templateEngine.process("mail/" + type.getTemplate(), context);
    }
}
