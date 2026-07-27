package com.bulletin.security;

import com.bulletin.entity.Teacher;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.repository.UserTeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserTeacherRepository userTeacherRepository;

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

    public Long getCurrentSchoolId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getSchoolId();
        }
        return null;
    }

    public Long requireSchoolId() {
        Long schoolId = getCurrentSchoolId();
        if (schoolId == null) {
            throw new SecurityException("École non définie pour l'utilisateur connecté");
        }
        return schoolId;
    }

    public void assertSchoolAccess(Long entitySchoolId) {
        if (isSuperAdmin()) {
            return;
        }
        Long schoolId = requireSchoolId();
        if (entitySchoolId == null || !entitySchoolId.equals(schoolId)) {
            throw new SecurityException("Accès refusé : ressource hors de votre école");
        }
    }

    /**
     * Règle section 7 : un professeur ne peut agir que sur ses propres affectations.
     * La direction a accès à tout.
     */
    public void assertTeacherOwnsAssignment(TeachingAssignment assignment) {
        if (isDirection()) {
            return;
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authentification requise");
        }
        Teacher teacher = assignment.getTeacher();
        if (teacher == null) {
            throw new SecurityException("Accès refusé : affectation sans professeur");
        }
        if (!userTeacherRepository.existsByUser_IdAndTeacher_Id(currentUserId, teacher.getId())) {
            throw new SecurityException("Accès refusé : vous ne pouvez agir que sur vos propres affectations");
        }
    }
}
