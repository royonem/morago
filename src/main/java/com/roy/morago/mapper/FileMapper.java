package com.roy.morago.mapper;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.entity.file.File;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileMapper {
    FileResponse toResponse(File file);
}
