package com.bulletin.mapper;

import com.bulletin.dto.curriculum.TeachingAssignmentRequest;
import com.bulletin.dto.curriculum.TeachingAssignmentResponse;
import com.bulletin.entity.TeachingAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeachingAssignmentMapper {

    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherNom", source = "teacher.nom")
    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomNom", source = "classroom.nom")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectNom", source = "subject.nom")
    TeachingAssignmentResponse toResponse(TeachingAssignment teachingAssignment);

    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    @Mapping(target = "subject", ignore = true)
    TeachingAssignment toEntity(TeachingAssignmentRequest request);

    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    @Mapping(target = "subject", ignore = true)
    void updateEntity(TeachingAssignmentRequest request, @MappingTarget TeachingAssignment teachingAssignment);
}
