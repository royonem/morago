package com.roy.morago.dto.file;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private String fileName;
    private String filePath;
    private String fileType;
    private String fileSize;
}
