package com.bulletin.config;

import com.bulletin.entity.Role;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import com.bulletin.security.JwtTokenProvider;
import com.bulletin.security.UserPrincipalService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private static final List<String> DEFAULT_ROLES = List.of(
            "SUPER_ADMIN",
            "ADMIN",
            "DIRECTEUR",
            "PREFET",
            "ENSEIGNANT"
    );

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findAll().stream().noneMatch(r -> r.getNom().equalsIgnoreCase(roleName))) {
                roleRepository.save(Role.builder().nom(roleName).build());
                log.info("Rôle par défaut créé: {}", roleName);
            }
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Utilisateur admin par défaut créé: admin / admin123");

            Role superAdmin = roleRepository.findAll().stream()
                    .filter(r -> "SUPER_ADMIN".equalsIgnoreCase(r.getNom()))
                    .findFirst()
                    .orElseThrow();
            userRoleRepository.save(UserRole.builder()
                    .user(admin)
                    .role(superAdmin)
                    .build());
            log.info("Rôle SUPER_ADMIN assigné à l'utilisateur admin par défaut");
        }
    }
}
