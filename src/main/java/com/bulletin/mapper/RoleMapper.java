package com.bulletin.mapper;

import com.bulletin.dto.user.RoleRequest;
import com.bulletin.dto.user.RoleResponse;
import com.bulletin.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    void updateEntity(RoleRequest request, @MappingTarget Role role);
}
