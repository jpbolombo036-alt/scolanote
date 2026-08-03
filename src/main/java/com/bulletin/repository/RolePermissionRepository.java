package com.bulletin.repository;

import com.bulletin.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    void deleteByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    /** Charge les associations d'un rôle avec leurs permissions (pour l'affichage). */
    @Query("""
            SELECT rp FROM RolePermission rp
            LEFT JOIN FETCH rp.permission
            WHERE rp.role.id = :roleId
            """)
    List<RolePermission> findByRoleIdWithPermission(@Param("roleId") Long roleId);
}
