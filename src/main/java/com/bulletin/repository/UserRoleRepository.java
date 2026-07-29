package com.bulletin.repository;

import com.bulletin.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    @Query("SELECT ur.role.nom FROM UserRole ur WHERE ur.user.id = :userId AND ur.role IS NOT NULL")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
}
