package com.roy.morago.dto.file;

public record FileResponse(
        long id,
        String fileName,
        String filePath,
        String fileType,
        Long fileSize
) {
}
