package com.roy.morago.mapper;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.entity.file.File;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {
    File createFileFromDto(FileDTO dto);
    FileDTO createFileDTOFromEntity(File file);
}
