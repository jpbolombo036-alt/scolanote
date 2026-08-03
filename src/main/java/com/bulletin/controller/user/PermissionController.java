package com.bulletin.controller.user;

import com.bulletin.entity.Permission;
import com.bulletin.entity.RolePermission;
import com.bulletin.repository.PermissionRepository;
import com.bulletin.repository.RolePermissionRepository;
import com.bulletin.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de lecture des permissions (RBAC).
 * Permet à l'interface d'administration d'afficher le catalogue des permissions
 * et les permissions associées à chaque rôle.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Consultation des permissions et des permissions par rôle")
public class PermissionController {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SecurityUtils securityUtils;

    @GetMapping("/permissions")
    @Operation(summary = "Catalogue des permissions", description = "Retourne toutes les permissions disponibles, groupées par catégorie")
    public ResponseEntity<List<Map<String, Object>>> getAllPermissions() {
        assertCanViewPermissions();
        List<Map<String, Object>> result = permissionRepository.findAll().stream()
                .map(this::toMap)
                .sorted((a, b) -> {
                    String ca = (String) a.get("categorie");
                    String cb = (String) b.get("categorie");
                    int cmp = (ca == null ? "" : ca).compareTo(cb == null ? "" : cb);
                    return cmp != 0 ? cmp : ((String) a.get("code")).compareTo((String) b.get("code"));
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Permissions d'un rôle", description = "Retourne les permissions associées à un rôle donné")
    public ResponseEntity<List<Map<String, Object>>> getPermissionsByRole(@PathVariable Long roleId) {
        assertCanViewPermissions();
        List<Map<String, Object>> result = rolePermissionRepository.findByRoleIdWithPermission(roleId).stream()
                .map(RolePermission::getPermission)
                .filter(p -> p != null)
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(Permission p) {
        return Map.of(
                "id", p.getId(),
                "code", p.getCode(),
                "libelle", p.getLibelle() != null ? p.getLibelle() : "",
                "categorie", p.getCategorie() != null ? p.getCategorie() : ""
        );
    }

    /** Seuls SUPER_ADMIN / ADMIN peuvent consulter le catalogue des permissions. */
    private void assertCanViewPermissions() {
        if (!securityUtils.isAdmin()) {
            throw new SecurityException("Accès refusé : réservé aux administrateurs");
        }
    }
}
