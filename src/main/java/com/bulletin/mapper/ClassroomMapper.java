package com.bulletin.mapper;

import com.bulletin.dto.school.ClassroomRequest;
import com.bulletin.dto.school.ClassroomResponse;
import com.bulletin.entity.Classroom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassroomMapper {

    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLibelle", source = "academicYear.libelle")
    @Mapping(target = "levelId", source = "level.id")
    @Mapping(target = "levelNom", source = "level.nom")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionNom", source = "section.nom")
    @Mapping(target = "optionId", source = "option.id")
    @Mapping(target = "optionNom", source = "option.nom")
    @Mapping(target = "reportTemplateId", source = "reportTemplate.id")
    @Mapping(target = "reportTemplateNom", source = "reportTemplate.nom")
    ClassroomResponse toResponse(Classroom classroom);

    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "option", ignore = true)
    @Mapping(target = "reportTemplate", ignore = true)
    Classroom toEntity(ClassroomRequest request);

    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "option", ignore = true)
    @Mapping(target = "reportTemplate", ignore = true)
    void updateEntity(ClassroomRequest request, @MappingTarget Classroom classroom);
}
