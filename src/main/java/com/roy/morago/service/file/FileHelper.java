package com.roy.morago.service.file;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.entity.file.File;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.enums.FileStatus;
import com.roy.morago.exception.FileNotFoundException;
import com.roy.morago.exception.FileValidationException;
import com.roy.morago.mapper.FileMapper;
import com.roy.morago.repository.file.FileRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileHelper {
    FileRepository fileRepository;
    FileStorageService fileStorageService;
    FileMapper fileMapper;
    private static final List<String> ALLOWED_TYPES =
            List.of("image/png", "image/jpeg");

    protected FileDTO uploadFile(MultipartFile file, FilePurpose filePurpose) {
        validateFile(file, filePurpose);
        String tempPath = fileStorageService.storeTempFile(file);
        File fileEntity = buildFileEntity(file, tempPath, filePurpose);
        fileRepository.save(fileEntity);
        return fileMapper.createFileDTOFromEntity(fileEntity);
    }

    protected File buildFileEntity(MultipartFile file, String tempPath, FilePurpose filePurpose) {
        File fileEntity = new File();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFilePurpose(filePurpose);
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileStatus(FileStatus.PENDING);
        fileEntity.setFilePath(tempPath);
        return fileEntity;
    }

    protected void validateFile(@NonNull MultipartFile file, FilePurpose filePurpose) {
        int profilePicSizeLimit = 5 * 1024 * 1024;
        int iconSizeLimit = 500 * 1024;

        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
        if ((FilePurpose.ICON.equals(filePurpose) && file.getSize() > iconSizeLimit)
                || (FilePurpose.PICTURE.equals(filePurpose) && file.getSize() > profilePicSizeLimit)) {
            throw new FileValidationException("File size is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new FileValidationException("Unsupported file type");
        }
    }

    protected void finalizeFile(File file, String finalPath) {
        file.setFilePath(finalPath);
        file.activate();
        fileRepository.save(file);
    }

    protected File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));
    }

    protected File findPictureByUser(User user) {
        return fileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new FileNotFoundException("Profile Picture not found."));
    }

    protected File findIconByTopic(Topic topic) {
        return fileRepository.findByTopicId(topic.getId())
                .orElseThrow(() -> new FileNotFoundException("Icon not found."));
    }
}
