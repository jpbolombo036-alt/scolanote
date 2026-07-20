package com.bulletin.controller.user;

import com.bulletin.dto.user.UserRoleRequest;
import com.bulletin.dto.user.UserRoleResponse;
import com.bulletin.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles-utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Liens user-rôle", description = "Association utilisateurs ↔ rôles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping
    @Operation(summary = "Associer un rôle", description = "Associe un rôle à un utilisateur")
    public ResponseEntity<UserRoleResponse> createUserRole(@Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.createUserRole(request));
    }

    @GetMapping
    @Operation(summary = "Liste des liens", description = "Retourne tous les liens user-rôle")
    public ResponseEntity<List<UserRoleResponse>> getAllUserRoles() {
        return ResponseEntity.ok(userRoleService.getAllUserRoles());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirer un rôle", description = "Retire un rôle d'un utilisateur")
    public ResponseEntity<Void> deleteUserRole(@PathVariable Long id) {
        userRoleService.deleteUserRole(id);
        return ResponseEntity.noContent().build();
    }
}
