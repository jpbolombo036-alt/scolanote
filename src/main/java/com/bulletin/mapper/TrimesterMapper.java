package com.bulletin.mapper;

import com.bulletin.dto.school.TrimesterRequest;
import com.bulletin.dto.school.TrimesterResponse;
import com.bulletin.entity.Trimester;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrimesterMapper {
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLibelle", source = "academicYear.libelle")
    TrimesterResponse toResponse(Trimester trimester);

    @Mapping(target = "academicYear", ignore = true)
    Trimester toEntity(TrimesterRequest request);

    @Mapping(target = "academicYear", ignore = true)
    void updateEntity(TrimesterRequest request, @MappingTarget Trimester trimester);
}
