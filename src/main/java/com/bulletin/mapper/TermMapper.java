package com.bulletin.mapper;

import com.bulletin.dto.school.TermRequest;
import com.bulletin.dto.school.TermResponse;
import com.bulletin.entity.Term;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TermMapper {

    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLibelle", source = "academicYear.libelle")
    TermResponse toResponse(Term term);

    @Mapping(target = "academicYear", ignore = true)
    Term toEntity(TermRequest request);

    @Mapping(target = "academicYear", ignore = true)
    void updateEntity(TermRequest request, @MappingTarget Term term);
}
