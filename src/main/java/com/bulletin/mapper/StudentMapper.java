package com.bulletin.mapper;

import com.bulletin.dto.people.StudentRequest;
import com.bulletin.dto.people.StudentResponse;
import com.bulletin.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    Student toEntity(StudentRequest request);

    StudentResponse toResponse(Student student);

    void updateEntity(StudentRequest request, @MappingTarget Student student);
}
