package com.bulletin.mapper;

import com.bulletin.dto.bulletin.AcademicYearReportCardDetailResponse;
import com.bulletin.dto.bulletin.AcademicYearReportCardResponse;
import com.bulletin.entity.AcademicYearReportCard;
import com.bulletin.entity.AcademicYearReportCardDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicYearReportCardMapper {

    @Mapping(target = "enrollmentId", source = "enrollment.id")
    @Mapping(target = "studentId", source = "enrollment.student.id")
    @Mapping(target = "studentNom", source = "enrollment.student.nom")
    @Mapping(target = "studentMatricule", source = "enrollment.student.matricule")
    @Mapping(target = "eleveNom", source = "enrollment.student.nom")
    @Mapping(target = "elevePrenom", source = "enrollment.student.prenom")
    @Mapping(target = "classroomId", source = "enrollment.classroom.id")
    @Mapping(target = "classroomNom", source = "enrollment.classroom.nom")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearNom", source = "academicYear.libelle")
    @Mapping(target = "details", source = "details")
    AcademicYearReportCardResponse toResponse(AcademicYearReportCard academicYearReportCard);

    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectNom", source = "subject.nom")
    @Mapping(target = "subjectCode", source = "subject.code")
    @Mapping(target = "appreciation", source = "observation")
    AcademicYearReportCardDetailResponse toDetailResponse(AcademicYearReportCardDetail detail);
}
