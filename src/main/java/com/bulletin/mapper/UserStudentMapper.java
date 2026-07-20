package com.bulletin.mapper;

import com.bulletin.dto.user.UserStudentRequest;
import com.bulletin.dto.user.UserStudentResponse;
import com.bulletin.entity.UserStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserStudentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentNom", source = "student.nom")
    UserStudentResponse toResponse(UserStudent userStudent);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "student", ignore = true)
    UserStudent toEntity(UserStudentRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateEntity(UserStudentRequest request, @MappingTarget UserStudent userStudent);
}
