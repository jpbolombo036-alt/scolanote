package com.bulletin.config;

import com.bulletin.entity.Role;
import com.bulletin.entity.Trimester;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.TrimesterRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import com.bulletin.security.JwtTokenProvider;
import com.bulletin.security.UserPrincipalService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final AcademicYearRepository academicYearRepository;
    private final TrimesterRepository trimesterRepository;

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
        } else {
            User admin = userRepository.findByUsername("admin").get();
            Role superAdmin = roleRepository.findAll().stream()
                    .filter(r -> "SUPER_ADMIN".equalsIgnoreCase(r.getNom()))
                    .findFirst()
                    .orElseThrow();
            boolean hasSuperAdmin = userRoleRepository.findAll().stream()
                    .anyMatch(ur -> ur.getUser().getId().equals(admin.getId())
                            && ur.getRole().getId().equals(superAdmin.getId()));
            if (!hasSuperAdmin) {
                userRoleRepository.save(UserRole.builder()
                        .user(admin)
                        .role(superAdmin)
                        .build());
                log.info("Rôle SUPER_ADMIN assigné à l'utilisateur admin existant");
            }
        }

        initPeriodes();
    }

    private void initPeriodes() {
        academicYearRepository.findAll().forEach(year -> {
            if (trimesterRepository.findByAcademicYearId(year.getId()).isEmpty()) {
                String[][] trimestresData = {
                        {"1er Trimestre", "1", "2025-09-01", "2025-12-15"},
                        {"2e Trimestre", "2", "2026-01-05", "2026-04-15"},
                        {"3e Trimestre", "3", "2026-04-16", "2026-09-15"}
                };

                for (String[] t : trimestresData) {
                    Trimester trimester = Trimester.builder()
                            .academicYear(year)
                            .nom(t[0])
                            .ordre(Integer.parseInt(t[1]))
                            .dateDebut(LocalDate.parse(t[2]))
                            .dateFin(LocalDate.parse(t[3]))
                            .build();
                    trimesterRepository.save(trimester);
                    log.info("Trimestre créé: {}", t[0]);
                }
            }
        });
    }
}
