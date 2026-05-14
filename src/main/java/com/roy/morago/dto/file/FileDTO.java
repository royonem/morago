package com.roy.morago.dto.file;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private Long fileId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
}
