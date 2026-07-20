package com.bulletin.mapper;

import com.bulletin.dto.people.EnrollmentRequest;
import com.bulletin.dto.people.EnrollmentResponse;
import com.bulletin.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrollmentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentNom", source = "student.nom")
    @Mapping(target = "studentMatricule", source = "student.matricule")
    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomNom", source = "classroom.nom")
    EnrollmentResponse toResponse(Enrollment enrollment);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    Enrollment toEntity(EnrollmentRequest request);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    void updateEntity(EnrollmentRequest request, @MappingTarget Enrollment enrollment);
}
