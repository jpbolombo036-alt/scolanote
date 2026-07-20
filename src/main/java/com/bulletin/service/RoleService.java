package com.bulletin.service;

import com.bulletin.dto.user.RoleRequest;
import com.bulletin.dto.user.RoleResponse;
import com.bulletin.entity.Role;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.RoleMapper;
import com.bulletin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        Role saved = roleRepository.save(role);
        log.info("Rôle créé: {}", saved.getId());
        return roleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(Long id) {
        return roleMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = findById(id);
        roleMapper.updateEntity(request, role);
        Role saved = roleRepository.save(role);
        log.info("Rôle mis à jour: {}", saved.getId());
        return roleMapper.toResponse(saved);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = findById(id);
        role.setDeletedAt(java.time.LocalDateTime.now());
        roleRepository.save(role);
        log.info("Rôle supprimé (soft): {}", id);
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec l'ID: " + id));
    }

    public Role findByNom(String nom) {
        return roleRepository.findAll().stream()
                .filter(r -> r.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElse(null);
    }
}
