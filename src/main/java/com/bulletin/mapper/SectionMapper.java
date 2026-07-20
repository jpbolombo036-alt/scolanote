package com.bulletin.mapper;

import com.bulletin.dto.school.SectionRequest;
import com.bulletin.dto.school.SectionResponse;
import com.bulletin.entity.Section;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SectionMapper {

    Section toEntity(SectionRequest request);

    SectionResponse toResponse(Section section);

    void updateEntity(SectionRequest request, @MappingTarget Section section);
}
