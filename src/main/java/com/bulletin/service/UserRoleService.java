package com.bulletin.service;

import com.bulletin.dto.user.UserRoleRequest;
import com.bulletin.dto.user.UserRoleResponse;
import com.bulletin.entity.Role;
import com.bulletin.entity.User;
import com.bulletin.entity.UserRole;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.UserRoleMapper;
import com.bulletin.repository.RoleRepository;
import com.bulletin.repository.UserRepository;
import com.bulletin.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;

    @Transactional
    public UserRoleResponse createUserRole(UserRoleRequest request) {
        UserRole userRole = userRoleMapper.toEntity(request);
        userRole.setUser(findUser(request.getUserId()));
        userRole.setRole(findRole(request.getRoleId()));
        UserRole saved = userRoleRepository.save(userRole);
        log.info("Lien user-role créé: {}", saved.getId());
        return userRoleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAllUserRoles() {
        return userRoleRepository.findAll().stream()
                .map(userRoleMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteUserRole(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lien user-role non trouvé avec l'ID: " + id));
        userRoleRepository.delete(userRole);
        log.info("Lien user-role supprimé: {}", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    private Role findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec l'ID: " + id));
    }
}
