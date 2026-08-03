package com.bulletin.repository;

import com.bulletin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);

    List<Permission> findByCategorie(String categorie);

    /**
     * Charge tous les codes de permission associés à un utilisateur (via ses rôles).
     * Utilisé pour enrichir le JWT et pour hasPermission().
     */
    @Query("""
            SELECT DISTINCT p.code FROM Permission p
            JOIN RolePermission rp ON rp.permission.id = p.id
            JOIN UserRole ur ON ur.role.id = rp.role.id
            WHERE ur.user.id = :userId
            """)
    List<String> findCodesByUserId(@Param("userId") Long userId);
}
