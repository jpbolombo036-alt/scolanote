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
 * Permet aux administrateurs de :
 *  - créer des rôles personnalisés (ex: SECRETAIRE, COMPTABLE)
 *  - assigner/retirer des permissions granulaires à chaque rôle
 * Réservé à SUPER_ADMIN et ADMIN.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Gestion des rôles", description = "Création de rôles et gestion de leurs permissions")
public class RolePermissionManagementController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SecurityUtils securityUtils;

    // === Rôles ===

    @GetMapping
    @Operation(summary = "Liste des rôles", description = "Retourne tous les rôles avec leur nombre de permissions")
    public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
        assertAdmin();
        List<Map<String, Object>> result = roleRepository.findAll().stream()
                .map(role -> {
                    long count = rolePermissionRepository.findByRoleId(role.getId()).size();
                    return Map.<String, Object>of(
                            "id", role.getId(),
                            "nom", role.getNom(),
                            "nombrePermissions", count
                    );
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Créer un rôle", description = "Crée un rôle personnalisé (ex: SECRETAIRE, COMPTABLE)")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Map<String, String> body) {
        assertAdmin();
        String nom = body.get("nom");
        if (nom == null || nom.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom du rôle est requis"));
        }
        String normalized = nom.trim().toUpperCase();
        if (roleRepository.findByNom(normalized).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Ce rôle existe déjà"));
        }
        Role saved = roleRepository.save(Role.builder().nom(normalized).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "nom", saved.getNom(),
                "message", "Rôle créé avec succès"
        ));
    }

    // === Permissions par rôle ===

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
