package com.bulletin.service;

import com.bulletin.dto.user.UserRequest;
import com.bulletin.dto.user.UserResponse;
import com.bulletin.entity.Role;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import com.bulletin.security.JwtTokenProvider;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final com.bulletin.notification.NotificationPublisher notificationPublisher;
    private final com.bulletin.notification.NotificationProperties notificationProperties;
    private final JwtTokenProvider jwtTokenProvider;

    /** Durée de validité du lien de réinitialisation initié par un admin : 24 heures. */
    private static final long ADMIN_RESET_TOKEN_EXPIRATION_MS = 86_400_000L; // 24h

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.isEnabled())
                .schoolId(securityUtils.getCurrentSchoolId())
                .build();
        User saved = userRepository.save(user);

        List<String> roleNames = request.getRoles() != null ? request.getRoles() : List.of("USER");
        for (String roleName : roleNames) {
            Role role = roleRepository.findAll().stream()
                    .filter(r -> r.getNom().equalsIgnoreCase(roleName))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName));
            userRoleRepository.save(UserRole.builder().user(saved).role(role).build());
        }

        log.info("Utilisateur créé: {}", saved.getId());

        // Événement : utilisateur créé → e-mail de bienvenue (asynchrone)
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            notificationPublisher.publish(com.bulletin.notification.NotificationEvent
                    .builder(com.bulletin.notification.NotificationType.USER_CREATED, saved.getEmail())
                    .recipientName(saved.getUsername())
                    .variable("username", saved.getUsername())
                    .variable("lien", notificationProperties.getFrontendUrl())
                    .referenceId(saved.getId())
                    .schoolId(saved.getSchoolId())
                    .build());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAccessibleUsers() {
        if (securityUtils.isSuperAdmin()) {
            return userRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }

        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            return List.of();
        }

        return userRepository.findAll().stream()
                .filter(user -> schoolId.equals(user.getSchoolId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findById(id);
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setEnabled(request.isEnabled());
        User saved = userRepository.save(user);

        // Remplacement des rôles
        userRoleRepository.findAll().stream()
                .filter(ur -> ur.getUser().getId().equals(saved.getId()))
                .toList()
                .forEach(userRoleRepository::delete);
        List<String> roleNames = request.getRoles() != null ? request.getRoles() : List.of("USER");
        for (String roleName : roleNames) {
            Role role = roleRepository.findAll().stream()
                    .filter(r -> r.getNom().equalsIgnoreCase(roleName))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName));
            userRoleRepository.save(UserRole.builder().user(saved).role(role).build());
        }

        log.info("Utilisateur mis à jour: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRoleRepository.findAll().stream()
                .filter(ur -> ur.getUser().getId().equals(id))
                .toList()
                .forEach(userRoleRepository::delete);
        userRepository.delete(user);
        log.info("Utilisateur supprimé: {}", id);
    }

    /**
     * Réinitialisation du mot de passe par un administrateur.
     *
     * L'admin ne voit ni ne définit jamais le mot de passe : un lien de réinitialisation
     * sécurisé (token JWT, valide 24h) est envoyé à l'adresse e-mail de l'utilisateur.
     * L'ancien mot de passe reste actif tant que l'utilisateur n'a pas utilisé le lien
     * (passwordResetRequired n'est PAS forcé — l'utilisateur n'est pas à l'origine de la demande).
     *
     * @param userId ID de l'utilisateur cible (doit appartenir à l'école de l'admin)
     * @throws IllegalArgumentException si l'utilisateur n'a pas d'adresse e-mail
     */
    @Transactional(readOnly = true)
    public void adminResetPassword(Long userId) {
        // findById inclut déjà assertSchoolAccess (isolation multi-école)
        User user = findById(userId);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Impossible de réinitialiser : cet utilisateur n'a pas d'adresse e-mail");
        }

        String resetToken = jwtTokenProvider.generateResetToken(user.getUsername(), ADMIN_RESET_TOKEN_EXPIRATION_MS);
        String resetLink = notificationProperties.getFrontendUrl() + "/reinitialiser-mot-de-passe?token=" + resetToken;

        // Événement : réinitialisation par un admin → e-mail avec lien (asynchrone)
        notificationPublisher.publish(com.bulletin.notification.NotificationEvent
                .builder(com.bulletin.notification.NotificationType.PASSWORD_RESET_BY_ADMIN, user.getEmail())
                .recipientName(user.getUsername())
                .variable("lien", resetLink)
                .variable("dureeExpiration", "24 heures")
                .variable("parAdmin", true)
                .referenceId(user.getId())
                .schoolId(user.getSchoolId())
                .build());

        log.info("Réinitialisation du mot de passe initiée par un admin pour l'utilisateur: {}", userId);
    }

    public User findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(user.getSchoolId());
        return user;
    }

    private UserResponse toResponse(User user) {
        List<String> roles = userRoleRepository.findAll().stream()
                .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(user.getId()) && ur.getRole() != null)
                .map(ur -> ur.getRole().getNom())
                .collect(Collectors.toList());
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .schoolId(user.getSchoolId())
                .build();
    }
}
