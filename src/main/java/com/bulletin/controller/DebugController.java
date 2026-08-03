package com.bulletin.controller;

import com.bulletin.repository.PermissionRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de débogage (réservés aux utilisateurs authentifiés en prod).
 * Utile pour vérifier l'authentification, les rôles et les permissions de l'utilisateur connecté.
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final SecurityUtils securityUtils;
    private final PermissionRepository permissionRepository;

    @GetMapping("/whoami")
    public ResponseEntity<Map<String, Object>> whoami() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("authenticated", authentication != null && authentication.isAuthenticated());
        result.put("principal", authentication != null ? authentication.getName() : null);
        result.put("authorities", authentication != null ? authentication.getAuthorities().stream().map(Object::toString).toList() : List.of());
        result.put("currentUserId", securityUtils.getCurrentUserId());
        result.put("isDirection", securityUtils.isDirection());

        // Permissions granulaires de l'utilisateur (via ses rôles) — utile pour tester le RBAC
        Long userId = securityUtils.getCurrentUserId();
        result.put("permissions", userId != null ? permissionRepository.findCodesByUserId(userId) : List.of());

        return ResponseEntity.ok(result);
    }
}
