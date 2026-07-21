package com.bulletin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public List<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring("ROLE_".length()) : a)
                .collect(Collectors.toList());
    }

    public boolean hasRole(String role) {
        String target = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentRoles().stream()
                .anyMatch(r -> ("ROLE_" + r).equals(target) || r.equals(target));
    }

    public boolean isAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN");
    }

    public boolean isDirection() {
        return isAdmin() || hasRole("DIRECTEUR") || hasRole("PREFET");
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    public boolean isAdminRole() {
        return hasRole("ADMIN");
    }

    public boolean isDirecteur() {
        return hasRole("DIRECTEUR");
    }

    public boolean isPrefet() {
        return hasRole("PREFET");
    }

    public boolean isEnseignant() {
        return hasRole("ENSEIGNANT");
    }
}
