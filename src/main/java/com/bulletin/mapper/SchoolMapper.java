package com.bulletin.mapper;

import com.bulletin.dto.school.SchoolRequest;
import com.bulletin.dto.school.SchoolResponse;
import com.bulletin.entity.School;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchoolMapper {

    School toEntity(SchoolRequest request);

    SchoolResponse toResponse(School school);

    void updateEntity(SchoolRequest request, @MappingTarget School school);
}
