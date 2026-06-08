package com.roy.morago.configs;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "file.upload")
public class FileProperties {
    private String tempDir;
    private String pictureDir;
    private String iconDir;
}
