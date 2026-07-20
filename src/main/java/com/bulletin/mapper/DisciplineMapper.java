package com.bulletin.mapper;

import com.bulletin.dto.tracking.DisciplineRequest;
import com.bulletin.dto.tracking.DisciplineResponse;
import com.bulletin.entity.Discipline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DisciplineMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentNom", source = "student.nom")
    @Mapping(target = "studentMatricule", source = "student.matricule")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "termNom", source = "term.nom")
    DisciplineResponse toResponse(Discipline discipline);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "term", ignore = true)
    Discipline toEntity(DisciplineRequest request);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "term", ignore = true)
    void updateEntity(DisciplineRequest request, @MappingTarget Discipline discipline);
}
