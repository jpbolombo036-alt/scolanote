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

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.isEnabled())
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
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(findById(id));
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

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    private UserResponse toResponse(User user) {
        List<String> roles = userRoleRepository.findAll().stream()
                .filter(ur -> ur.getUser().getId().equals(user.getId()))
                .map(ur -> ur.getRole().getNom())
                .collect(Collectors.toList());
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
