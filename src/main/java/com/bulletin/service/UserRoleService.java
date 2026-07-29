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
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SecurityUtils securityUtils;

    private boolean isSuperAdmin() {
        return securityUtils.isSuperAdmin();
    }

    private Long requireSchoolId() {
        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            throw new SecurityException("École non définie pour l'utilisateur connecté");
        }
        return schoolId;
    }

    @Transactional
    public UserRoleResponse createUserRole(UserRoleRequest request) {
        UserRole userRole = userRoleMapper.toEntity(request);
        userRole.setSchoolId(requireSchoolId());
        userRole.setUser(findUser(request.getUserId()));
        userRole.setRole(findRole(request.getRoleId()));
        UserRole saved = userRoleRepository.save(userRole);
        log.info("Lien user-role créé: {}", saved.getId());
        return userRoleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserRoleResponse> getAccessibleUserRoles(Pageable pageable) {
        if (isSuperAdmin()) {
            return userRoleRepository.findAll(pageable)
                    .map(userRole -> {
                        if (userRole.getUser() == null || userRole.getRole() == null) {
                            return null;
                        }
                        return userRoleMapper.toResponse(userRole);
                    });
        }
        return userRoleRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(userRole -> {
                    if (userRole.getUser() == null || userRole.getRole() == null) {
                        return null;
                    }
                    return userRoleMapper.toResponse(userRole);
                });
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAccessibleUserRoles() {
        if (isSuperAdmin()) {
            return userRoleRepository.findAll().stream()
                    .map(userRole -> {
                        if (userRole.getUser() == null || userRole.getRole() == null) {
                            return null;
                        }
                        return userRoleMapper.toResponse(userRole);
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return userRoleRepository.findBySchoolId(requireSchoolId()).stream()
                .map(userRole -> {
                    if (userRole.getUser() == null || userRole.getRole() == null) {
                        return null;
                    }
                    return userRoleMapper.toResponse(userRole);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public void deleteUserRole(Long id) {
        UserRole userRole = findById(id);
        userRoleRepository.delete(userRole);
        log.info("Lien user-role supprimé: {}", id);
    }

    public UserRole findById(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lien user-role non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(userRole.getSchoolId());
        return userRole;
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
