package com.bulletin.mapper;

import com.bulletin.dto.curriculum.CurriculumRequest;
import com.bulletin.dto.curriculum.CurriculumResponse;
import com.bulletin.entity.Curriculum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurriculumMapper {

    @Mapping(target = "levelId", source = "level.id")
    @Mapping(target = "levelNom", source = "level.nom")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionNom", source = "section.nom")
    @Mapping(target = "optionId", source = "option.id")
    @Mapping(target = "optionNom", source = "option.nom")
    CurriculumResponse toResponse(Curriculum curriculum);

    @Mapping(target = "level", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "option", ignore = true)
    Curriculum toEntity(CurriculumRequest request);

    @Mapping(target = "level", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "option", ignore = true)
    void updateEntity(CurriculumRequest request, @MappingTarget Curriculum curriculum);
}
