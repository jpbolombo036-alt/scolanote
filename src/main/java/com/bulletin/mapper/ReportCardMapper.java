package com.bulletin.mapper;

import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.entity.ReportCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportCardMapper {

    @Mapping(target = "enrollmentId", source = "enrollment.id")
    @Mapping(target = "studentId", source = "enrollment.student.id")
    @Mapping(target = "studentNom", source = "enrollment.student.nom")
    @Mapping(target = "studentMatricule", source = "enrollment.student.matricule")
    @Mapping(target = "classroomId", source = "enrollment.classroom.id")
    @Mapping(target = "classroomNom", source = "enrollment.classroom.nom")
    @Mapping(target = "periodId", source = "period.id")
    @Mapping(target = "periodNom", source = "period.nom")
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "totalAbsences", source = "totalAbsences")
    @Mapping(target = "totalRetards", source = "totalRetards")
    @Mapping(target = "conduite", source = "conduite")
    @Mapping(target = "application", source = "application")
    ReportCardResponse toResponse(ReportCard reportCard);
}
