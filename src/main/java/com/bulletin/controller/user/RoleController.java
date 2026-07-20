package com.bulletin.controller.user;

import com.bulletin.dto.user.RoleRequest;
import com.bulletin.dto.user.RoleResponse;
import com.bulletin.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Rôles", description = "Gestion des rôles (SUPER_ADMIN, ADMIN, ...)")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Créer un rôle", description = "Crée un rôle")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rôle par ID", description = "Retourne un rôle par son ID")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRole(id));
    }

    @GetMapping
    @Operation(summary = "Liste des rôles", description = "Retourne tous les rôles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un rôle", description = "Modifie un rôle")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un rôle", description = "Supprime un rôle")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
