package com.bulletin.mapper;

import com.bulletin.dto.people.TeacherRequest;
import com.bulletin.dto.people.TeacherResponse;
import com.bulletin.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeacherMapper {

    Teacher toEntity(TeacherRequest request);

    TeacherResponse toResponse(Teacher teacher);

    void updateEntity(TeacherRequest request, @MappingTarget Teacher teacher);
}
