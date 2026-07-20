package com.bulletin.mapper;

import com.bulletin.dto.school.OptionRequest;
import com.bulletin.dto.school.OptionResponse;
import com.bulletin.entity.Option;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OptionMapper {

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionNom", source = "section.nom")
    OptionResponse toResponse(Option option);

    @Mapping(target = "section", ignore = true)
    Option toEntity(OptionRequest request);

    @Mapping(target = "section", ignore = true)
    void updateEntity(OptionRequest request, @MappingTarget Option option);
}
