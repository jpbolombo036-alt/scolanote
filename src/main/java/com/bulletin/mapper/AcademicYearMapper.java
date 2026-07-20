package com.bulletin.mapper;

import com.bulletin.dto.school.AcademicYearRequest;
import com.bulletin.dto.school.AcademicYearResponse;
import com.bulletin.entity.AcademicYear;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicYearMapper {

    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "schoolNom", source = "school.nom")
    AcademicYearResponse toResponse(AcademicYear academicYear);

    @Mapping(target = "school", ignore = true)
    AcademicYear toEntity(AcademicYearRequest request);

    @Mapping(target = "school", ignore = true)
    void updateEntity(AcademicYearRequest request, @MappingTarget AcademicYear academicYear);
}
