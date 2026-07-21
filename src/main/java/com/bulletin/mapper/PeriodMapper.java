package com.bulletin.mapper;

import com.bulletin.dto.school.PeriodRequest;
import com.bulletin.dto.school.PeriodResponse;
import com.bulletin.entity.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeriodMapper {
    @Mapping(target = "trimesterId", source = "trimester.id")
    @Mapping(target = "trimesterNom", source = "trimester.nom")
    @Mapping(target = "verrouille", source = "verrouille")
    @Mapping(target = "dateVerrouillage", source = "dateVerrouillage")
    @Mapping(target = "verrouillePar", source = "verrouillePar")
    PeriodResponse toResponse(Period period);

    @Mapping(target = "trimester", ignore = true)
    Period toEntity(PeriodRequest request);

    @Mapping(target = "trimester", ignore = true)
    void updateEntity(PeriodRequest request, @MappingTarget Period period);
}
