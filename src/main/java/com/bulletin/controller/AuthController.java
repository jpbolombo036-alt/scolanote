package com.bulletin.controller;

import com.bulletin.dto.*;
import com.bulletin.dto.auth.RegisterAgentRequest;
import com.bulletin.dto.auth.RegisterAgentResponse;
import com.bulletin.entity.Role;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import com.bulletin.security.JwtTokenProvider;
import com.bulletin.security.UserPrincipalService;
import com.bulletin.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserPrincipalService userPrincipalService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Value("${app.security.admin-init-key}")
    private String adminInitKey;

    @GetMapping("/status")
    @Operation(summary = "État du service", description = "Endpoint public pour le healthcheck de Railway")
    public ResponseEntity<String> getStatus() {
        log.debug("Healthcheck call received");
        return ResponseEntity.ok("UP");
    }

    @PostMapping("/register-agent")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Créer un utilisateur (agent)", description = "Crée un utilisateur avec un rôle spécifique")
    public ResponseEntity<RegisterAgentResponse> registerAgent(@Valid @RequestBody RegisterAgentRequest request) {
        if (request.getUsername() == null && request.getEmail() == null && request.getTelephone() == null) {
            return ResponseEntity.badRequest()
                    .body(RegisterAgentResponse.builder()
                            .message("Au moins un identifiant (username, email ou telephone) est requis")
                            .build());
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(RegisterAgentResponse.builder()
                            .message("Le rôle est requis")
                            .build());
        }

        Role role = roleRepository.findAll().stream()
                .filter(r -> r.getNom().equalsIgnoreCase(request.getRole()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().nom(request.getRole().toUpperCase()).build()));

        String username = request.getUsername();
        String email = request.getEmail();
        String telephone = request.getTelephone();

        if (username != null && userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(RegisterAgentResponse.builder()
                            .message("Ce username existe déjà")
                            .build());
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(RegisterAgentResponse.builder()
                            .message("Cet email existe déjà")
                            .build());
        }
        if (telephone != null && userRepository.findByTelephone(telephone).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(RegisterAgentResponse.builder()
                            .message("Ce téléphone existe déjà")
                            .build());
        }

        String password = request.getPassword();
        if (password == null || password.isBlank()) {
            password = "123456";
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .telephone(telephone)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .build();

        user = userRepository.save(user);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RegisterAgentResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .telephone(user.getTelephone())
                        .role(role.getNom())
                        .message("Utilisateur créé avec succès. Mot de passe: " + password)
                        .build());
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Connexion JSON", description = "Authentifie l'utilisateur avec un JSON et retourne un token JWT")
    public ResponseEntity<?> loginJson(@Valid @RequestBody LoginRequest requestBody) {
        return processLogin(requestBody.getUsername(), requestBody.getPassword());
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Connexion formulaire", description = "Authentifie l'utilisateur avec un formulaire URL-encoded et retourne un token JWT")
    public ResponseEntity<?> loginForm(@Valid @ModelAttribute LoginRequest requestBody) {
        return processLogin(requestBody.getUsername(), requestBody.getPassword());
    }

    private ResponseEntity<?> processLogin(String user, String pass) {
        if (user == null || pass == null || user.isBlank() || pass.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .error("Nom d'utilisateur et mot de passe requis")
                            .build());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user, pass)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User existingUser = userRepository.findByUsernameOrEmailOrTelephone(user, user, user)
                    .orElse(null);

            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.builder()
                                .error("Utilisateur non trouvé")
                                .build());
            }

            if (!existingUser.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.builder()
                                .error("Compte désactivé. Veuillez contacter l'administrateur.")
                                .build());
            }

            String token = jwtTokenProvider.generateToken(
                    (com.bulletin.security.UserPrincipal) userPrincipalService.loadUserById(existingUser.getId())
            );

            return ResponseEntity.ok(TokenResponse.builder()
                    .accessToken(token)
                    .tokenType("bearer")
                    .build());

        } catch (AuthenticationException e) {
            log.warn("Échec d'authentification pour l'utilisateur: {}", user, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .error("Nom d'utilisateur ou mot de passe incorrect")
                            .build());
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification pour l'utilisateur: {}", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("Erreur serveur lors de la tentative de connexion")
                            .build());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Utilisateur actuel", description = "Retourne les informations de l'utilisateur actuellement connecté")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return ResponseEntity.ok(CurrentUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .build());
    }

    @PostMapping("/init-admin")
    @Operation(summary = "Initialiser l'admin", description = "Crée l'utilisateur admin initial (à utiliser uniquement pour la première configuration)")
    public ResponseEntity<String> initAdmin(@RequestParam(required = false) String initKey) {
        if (initKey == null || !initKey.equals(adminInitKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Clé d'initialisation invalide");
        }

        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User newAdmin = User.builder()
                            .username("admin")
                            .password(passwordEncoder.encode("admin123"))
                            .enabled(true)
                            .build();
                    return userRepository.save(newAdmin);
                });

        Role superAdminRole = roleRepository.findAll().stream()
                .filter(r -> "SUPER_ADMIN".equalsIgnoreCase(r.getNom()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().nom("SUPER_ADMIN").build()));

        boolean alreadyAssigned = userRoleRepository.findAll().stream()
                .anyMatch(ur -> ur.getUser().getId().equals(admin.getId())
                        && ur.getRole().getId().equals(superAdminRole.getId()));

        if (!alreadyAssigned) {
            userRoleRepository.save(UserRole.builder()
                    .user(admin)
                    .role(superAdminRole)
                    .build());
            return ResponseEntity.ok("Admin initialisé. Username: admin, Password: admin123. CHANGEZ CE MOT DE PASSE IMMÉDIATEMENT !");
        }

        return ResponseEntity.ok("Admin déjà initialisé. Username: admin");
    }

    @PostMapping("/mot-de-passe-oublie")
    @Operation(summary = "Demande de réinitialisation", description = "Envoie un email de réinitialisation de mot de passe")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        userRepository.findByUsername(request.getEmail()).ifPresent(user -> {
            String resetToken = jwtTokenProvider.generateResetToken(user.getUsername());
            emailService.sendPasswordResetEmail(user.getUsername(), user.getUsername(), resetToken);
        });

        return ResponseEntity.ok("Si ce compte existe, un lien de réinitialisation a été envoyé");
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe avec le token reçu")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirm confirm) {
        try {
            String username = jwtTokenProvider.validateResetToken(confirm.getToken());

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

            user.setPassword(passwordEncoder.encode(confirm.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Token invalide ou expiré");
        }
    }

    @PostMapping("/activer-utilisateur")
    @Operation(summary = "Activer un utilisateur", description = "Active un utilisateur par son username (réservé aux admins)")
    public ResponseEntity<String> activateUser(@RequestParam String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !hasAdminRole(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : droits administrateur requis");
        }

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");
        }

        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("Utilisateur " + username + " activé avec succès");
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_admin".equals(auth.getAuthority()) || "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}
