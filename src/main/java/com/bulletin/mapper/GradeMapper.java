package com.bulletin.mapper;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.entity.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentNom", source = "student.nom")
    @Mapping(target = "studentMatricule", source = "student.matricule")
    GradeResponse toResponse(Grade grade);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "student", ignore = true)
    Grade toEntity(GradeRequest request);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateEntity(GradeRequest request, @MappingTarget Grade grade);
}
