package com.bulletin.notification;

/**
 * Abstraction du fournisseur d'e-mail (Strategy pattern).
 *
 * Permet de changer de fournisseur (SMTP, SendGrid, Mailgun, API...) sans modifier
 * la logique métier (NotificationService). Respecte le principe Open/Closed :
 * un nouveau fournisseur = une nouvelle implémentation de cette interface.
 *
 * Une implémentation ne doit JAMAIS lever d'exception vers l'appelant :
 * elle retourne un résultat indiquant le succès ou l'échec (+ message d'erreur).
 */
public interface EmailProvider {

    /**
     * Envoie un e-mail HTML.
     *
     * @param to      adresse du destinataire
     * @param subject sujet de l'e-mail
     * @param htmlBody contenu HTML (déjà rendu par le template)
     * @return le résultat de l'envoi (succès ou échec + message d'erreur)
     */
    EmailSendResult sendHtmlEmail(String to, String subject, String htmlBody);

    /** Nom du fournisseur (ex: "smtp") — pour la traçabilité. */
    String getProviderName();

    /**
     * Résultat d'un envoi d'e-mail (succès ou échec + message).
     * Ne lève jamais d'exception (garantit que la transaction métier n'est pas annulée).
     */
    record EmailSendResult(boolean success, String errorMessage) {
        public static EmailSendResult ok() {
            return new EmailSendResult(true, null);
        }

        public static EmailSendResult failure(String errorMessage) {
            return new EmailSendResult(false, errorMessage);
        }
    }
}
