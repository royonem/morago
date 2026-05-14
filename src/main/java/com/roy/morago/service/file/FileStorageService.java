package com.roy.morago.service.file;

import com.roy.morago.configs.FileProperties;
import com.roy.morago.entity.file.File;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final FileProperties fileProperties;

    public String storeTempFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path uploadDir = Paths.get(fileProperties.getTempDir());
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    private String resolveFinalFolder(FilePurpose purpose) {
        return switch (purpose) {
            case PICTURE -> fileProperties.getPictureDir();
            case ICON -> fileProperties.getIconDir();
        };
    }

    public String moveToFinalStorage(File file, FilePurpose purpose) {
        try {
            Path oldPath = Paths.get(file.getFilePath());
            String fileName = oldPath.getFileName().toString();
            Path newDir = Paths.get(resolveFinalFolder(purpose));
            Files.createDirectories(newDir);
            Path newPath = newDir.resolve(fileName);
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return newPath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to move file", e);
        }
    }

    public void deleteFromStorage(String path) {
        try {
            Path filePath = Paths.get(path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file from storage", e);
        }
    }
}
