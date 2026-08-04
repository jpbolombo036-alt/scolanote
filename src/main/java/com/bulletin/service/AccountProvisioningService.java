package com.bulletin.service;

import com.bulletin.entity.Role;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.notification.NotificationEvent;
import com.bulletin.notification.NotificationPublisher;
import com.bulletin.notification.NotificationProperties;
import com.bulletin.notification.NotificationType;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de provisionnement de comptes utilisateurs.
 *
 * Centralise la création d'un compte User "simple" pour une personne (professeur, parent, agent) :
 *  - username = email
 *  - mot de passe par défaut = 12345678
 *  - passwordResetRequired = true (l'utilisateur DOIT changer son mot de passe à la 1ère connexion)
 *  - attribution du rôle
 *  - envoi d'un e-mail de bienvenue (asynchrone, via le module Notification)
 *
 * Réutilisé par TeacherService, StudentService (et potentiellement d'autres).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountProvisioningService {

    /** Mot de passe par défaut attribué à la création d'un compte (changé à la 1ère connexion). */
    public static final String DEFAULT_PASSWORD = "12345678";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationPublisher notificationPublisher;
    private final NotificationProperties notificationProperties;

    /**
     * Crée un compte utilisateur simple pour une personne.
     *
     * @param email      l'email (devient le username) — obligatoire
     * @param roleName   le rôle à attribuer (ex: ENSEIGNANT, PARENT)
     * @param schoolId   l'école (contexte multi-tenant)
     * @param recipientName le nom d'affichage pour l'e-mail de bienvenue
     * @return le User créé (ou existant si l'email était déjà utilisé)
     */
    @Transactional
    public User provisionAccount(String email, String roleName, Long schoolId, String recipientName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est requis pour créer un compte utilisateur");
        }

        // Si un compte existe déjà avec cet email, on le retourne sans rien recréer
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            ensureRole(user, roleName);
            log.info("Compte existant réutilisé pour l'email {} (userId={})", email, user.getId());
            return user;
        }

        // Créer le compte : username=email, mdp par défaut, changement obligatoire à la 1ère connexion
        User user = User.builder()
                .username(email)
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .enabled(true)
                .passwordResetRequired(true)
                .schoolId(schoolId)
                .build();
        user = userRepository.save(user);

        ensureRole(user, roleName);

        // E-mail de bienvenue (asynchrone — n'affecte jamais la réponse)
        sendWelcomeEmail(user, recipientName);

        log.info("Compte provisionné pour l'email {} avec le rôle {} (userId={})", email, roleName, user.getId());
        return user;
    }

    /** Attribue le rôle au user s'il ne l'a pas déjà. */
    private void ensureRole(User user, String roleName) {
        Role role = roleRepository.findByNom(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().nom(roleName).build()));

        boolean alreadyAssigned = userRoleRepository.findAll().stream()
                .anyMatch(ur -> ur.getUser() != null && ur.getUser().getId().equals(user.getId())
                        && ur.getRole() != null && ur.getRole().getId().equals(role.getId()));

        if (!alreadyAssigned) {
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());
        }
    }

    /** Envoie l'e-mail de bienvenue avec le mot de passe temporaire. */
    private void sendWelcomeEmail(User user, String recipientName) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        notificationPublisher.publish(NotificationEvent
                .builder(NotificationType.USER_CREATED, user.getEmail())
                .recipientName(recipientName != null ? recipientName : user.getUsername())
                .variable("username", user.getUsername())
                .variable("lien", notificationProperties.getFrontendUrl())
                .referenceId(user.getId())
                .schoolId(user.getSchoolId())
                .build());
    }
}
