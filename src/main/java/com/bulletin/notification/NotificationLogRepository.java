package com.bulletin.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByRecipient(String recipient);
    List<NotificationLog> findByType(NotificationType type);
    List<NotificationLog> findByStatus(NotificationLog.Status status);
    List<NotificationLog> findByReferenceId(Long referenceId);
}
