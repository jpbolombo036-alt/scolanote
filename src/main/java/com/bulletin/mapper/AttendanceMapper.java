package com.bulletin.mapper;

import com.bulletin.dto.tracking.AttendanceRequest;
import com.bulletin.dto.tracking.AttendanceResponse;
import com.bulletin.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentNom", source = "student.nom")
    @Mapping(target = "studentMatricule", source = "student.matricule")
    AttendanceResponse toResponse(Attendance attendance);

    @Mapping(target = "student", ignore = true)
    Attendance toEntity(AttendanceRequest request);

    @Mapping(target = "student", ignore = true)
    void updateEntity(AttendanceRequest request, @MappingTarget Attendance attendance);
}
