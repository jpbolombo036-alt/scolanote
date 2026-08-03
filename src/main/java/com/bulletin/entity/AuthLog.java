package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Journal des tentatives d'authentification.
 * Hérite de BaseEntity : created_at est géré automatiquement (horodatage du log),
 * deleted_at/updated_at existent en base (migration V21).
 */
@Entity
@Table(name = "auth_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthLog extends BaseEntity {
    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error_reason", length = 500)
    private String errorReason;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
