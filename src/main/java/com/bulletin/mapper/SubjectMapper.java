package com.bulletin.mapper;

import com.bulletin.dto.curriculum.SubjectRequest;
import com.bulletin.dto.curriculum.SubjectResponse;
import com.bulletin.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    Subject toEntity(SubjectRequest request);

    SubjectResponse toResponse(Subject subject);

    void updateEntity(SubjectRequest request, @MappingTarget Subject subject);
}
