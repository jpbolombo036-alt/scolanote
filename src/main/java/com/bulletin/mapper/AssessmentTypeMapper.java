package com.bulletin.mapper;

import com.bulletin.dto.grade.AssessmentTypeRequest;
import com.bulletin.dto.grade.AssessmentTypeResponse;
import com.bulletin.entity.AssessmentType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssessmentTypeMapper {

    AssessmentType toEntity(AssessmentTypeRequest request);

    AssessmentTypeResponse toResponse(AssessmentType assessmentType);

    void updateEntity(AssessmentTypeRequest request, @MappingTarget AssessmentType assessmentType);
}
