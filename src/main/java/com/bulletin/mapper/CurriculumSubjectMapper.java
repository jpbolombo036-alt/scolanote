package com.bulletin.mapper;

import com.bulletin.dto.curriculum.CurriculumSubjectRequest;
import com.bulletin.dto.curriculum.CurriculumSubjectResponse;
import com.bulletin.entity.CurriculumSubject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurriculumSubjectMapper {

    @Mapping(target = "curriculumId", source = "curriculum.id")
    @Mapping(target = "curriculumNom", source = "curriculum.nom")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectNom", source = "subject.nom")
    @Mapping(target = "subjectCode", source = "subject.code")
    CurriculumSubjectResponse toResponse(CurriculumSubject curriculumSubject);

    @Mapping(target = "curriculum", ignore = true)
    @Mapping(target = "subject", ignore = true)
    CurriculumSubject toEntity(CurriculumSubjectRequest request);

    @Mapping(target = "curriculum", ignore = true)
    @Mapping(target = "subject", ignore = true)
    void updateEntity(CurriculumSubjectRequest request, @MappingTarget CurriculumSubject curriculumSubject);
}
