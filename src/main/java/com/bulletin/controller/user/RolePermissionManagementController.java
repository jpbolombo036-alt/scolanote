package com.bulletin.controller.user;

import com.bulletin.entity.Permission;
import com.bulletin.entity.Role;
import com.bulletin.entity.RolePermission;
import com.bulletin.repository.PermissionRepository;
import com.bulletin.repository.RolePermissionRepository;
import com.bulletin.repository.RoleRepository;
import com.bulletin.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Gestion des permissions par rôle (Phase 3 du RBAC).
 * Permet aux administrateurs d'assigner/retirer des permissions granulaires à chaque rôle.
 * Réservé à SUPER_ADMIN et ADMIN.
 *
 * Note : le CRUD des rôles (créer, lister, modifier, supprimer) est déjà géré par
 * {@link RoleController} sur /api/roles. Ce contrôleur ne gère QUE les permissions par rôle
 * pour éviter tout mapping ambigu.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Permissions par rôle", description = "Assignation/retrait de permissions aux rôles")
public class RolePermissionManagementController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SecurityUtils securityUtils;

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Assigner une permission", description = "Attribue une permission à un rôle")
    public ResponseEntity<Map<String, Object>> assignPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        assertAdmin();
        Role role = roleRepository.findById(roleId)
                .orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Rôle non trouvé"));
        }
        Permission permission = permissionRepository.findById(permissionId)
                .orElse(null);
        if (permission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Permission non trouvée"));
        }
        if (rolePermissionRepository.existsByRole_IdAndPermission_Id(roleId, permissionId)) {
            return ResponseEntity.ok(Map.of("message", "Permission déjà assignée à ce rôle"));
        }
        rolePermissionRepository.save(RolePermission.builder()
                .role(role)
                .permission(permission)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Permission " + permission.getCode() + " assignée au rôle " + role.getNom()
        ));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Retirer une permission", description = "Retire une permission d'un rôle")
    public ResponseEntity<Map<String, Object>> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        assertAdmin();
        if (!rolePermissionRepository.existsByRole_IdAndPermission_Id(roleId, permissionId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cette permission n'est pas assignée à ce rôle"));
        }
        rolePermissionRepository.deleteByRole_IdAndPermission_Id(roleId, permissionId);
        return ResponseEntity.ok(Map.of("message", "Permission retirée du rôle"));
    }

    /** Seuls SUPER_ADMIN / ADMIN peuvent gérer les rôles et permissions. */
    private void assertAdmin() {
        if (!securityUtils.isAdmin()) {
            throw new SecurityException("Accès refusé : réservé aux administrateurs");
        }
    }
}
