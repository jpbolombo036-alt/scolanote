package com.bulletin.mapper;

import com.bulletin.dto.school.LevelRequest;
import com.bulletin.dto.school.LevelResponse;
import com.bulletin.entity.Level;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LevelMapper {

    Level toEntity(LevelRequest request);

    LevelResponse toResponse(Level level);

    void updateEntity(LevelRequest request, @MappingTarget Level level);
}
