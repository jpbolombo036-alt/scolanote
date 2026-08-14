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
    @Mapping(target = "matiere", expression = "java(grade.getAssessment() != null && grade.getAssessment().getAssignment() != null && grade.getAssessment().getAssignment().getSubject() != null ? grade.getAssessment().getAssignment().getSubject().getNom() : null)")
    @Mapping(target = "coefficient", expression = "java(grade.getAssessment() != null && grade.getAssessment().getAssignment() != null && grade.getAssessment().getAssignment().getSubject() != null ? grade.getAssessment().getAssignment().getSubject().getCoefficient() : null)")
    GradeResponse toResponse(Grade grade);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "student", ignore = true)
    Grade toEntity(GradeRequest request);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateEntity(GradeRequest request, @MappingTarget Grade grade);
}
