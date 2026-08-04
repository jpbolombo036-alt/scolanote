package com.bulletin.notification;

import com.bulletin.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Journal d'une notification envoyée (entité persistante).
 *
 * Trace chaque envoi d'e-mail : date, destinataire, type, statut (SUCCESS/FAILED),
 * message d'erreur éventuel. Permet l'audit et le diagnostic des notifications.
 */
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog extends BaseEntity {

    public enum Status {
        SUCCESS, FAILED
    }

    public enum Channel {
        EMAIL, SMS, PUSH
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Channel channel = Channel.EMAIL;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(length = 300)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /** Identifiant métier lié (ex: userId, reportCardId) — traçabilité (optionnel). */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Contexte multi-tenant (optionnel). */
    @Column(name = "school_id")
    private Long schoolId;
}
