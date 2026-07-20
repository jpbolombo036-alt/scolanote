package com.bulletin.mapper;

import com.bulletin.dto.user.UserRoleRequest;
import com.bulletin.dto.user.UserRoleResponse;
import com.bulletin.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRoleMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleNom", source = "role.nom")
    UserRoleResponse toResponse(UserRole userRole);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserRole toEntity(UserRoleRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntity(UserRoleRequest request, @MappingTarget UserRole userRole);
}
