package com.bulletin.mapper;

import com.bulletin.dto.grade.AssessmentRequest;
import com.bulletin.dto.grade.AssessmentResponse;
import com.bulletin.entity.Assessment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssessmentMapper {

    @Mapping(target = "assessmentTypeId", source = "assessmentType.id")
    @Mapping(target = "assessmentTypeNom", source = "assessmentType.nom")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "termNom", source = "term.nom")
    AssessmentResponse toResponse(Assessment assessment);

    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "assessmentType", ignore = true)
    @Mapping(target = "term", ignore = true)
    Assessment toEntity(AssessmentRequest request);

    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "assessmentType", ignore = true)
    @Mapping(target = "term", ignore = true)
    void updateEntity(AssessmentRequest request, @MappingTarget Assessment assessment);
}
