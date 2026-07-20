package com.bulletin.mapper;

import com.bulletin.dto.user.UserTeacherRequest;
import com.bulletin.dto.user.UserTeacherResponse;
import com.bulletin.entity.UserTeacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserTeacherMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherNom", source = "teacher.nom")
    UserTeacherResponse toResponse(UserTeacher userTeacher);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    UserTeacher toEntity(UserTeacherRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    void updateEntity(UserTeacherRequest request, @MappingTarget UserTeacher userTeacher);
}
