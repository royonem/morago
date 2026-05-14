package com.roy.morago.service.file;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.entity.file.File;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.enums.FileStatus;
import com.roy.morago.exception.FileNotFoundException;
import com.roy.morago.exception.FileValidationException;
import com.roy.morago.exception.UserNotFoundException;
import com.roy.morago.mapper.FileMapper;
import com.roy.morago.repository.file.FileRepository;
import com.roy.morago.repository.user.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private static final List<String> ALLOWED_TYPES =
            List.of("image/png", "image/jpeg");

    @Transactional
    public FileDTO uploadFile(MultipartFile file, FilePurpose filePurpose) {
        validateFile(file, filePurpose);
        String tempPath = fileStorageService.storeTempFile(file);
        File fileEntity = buildFileEntity(file, tempPath, filePurpose);
        fileRepository.save(fileEntity);
        return fileMapper.createFileDTOFromEntity(fileEntity);
    }

    public File buildFileEntity(MultipartFile file, String tempPath, FilePurpose filePurpose) {
        File fileEntity = new File();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFilePurpose(filePurpose);
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileStatus(FileStatus.PENDING);
        fileEntity.setFilePath(tempPath);
        return fileEntity;
    }

    public void validateFile(@NonNull MultipartFile file, FilePurpose filePurpose) {
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

    @Transactional(readOnly = true)
    public FileDTO viewFile(Long id) {
        return fileMapper.createFileDTOFromEntity(findFileById(id));
    }

    @Transactional
    public void deleteFile(Long id) {
        File file = findFileById(id);
        fileRepository.deleteById(id);
        fileStorageService.deleteFromStorage(file.getFilePath());
    }

    @Transactional  // possibly move this to UserController later
    public void saveProfilePicture(Long pictureId, Authentication authentication) {
        File file = findFileById(pictureId);

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));

        String finalPath = fileStorageService.moveToFinalStorage(file, FilePurpose.PICTURE);
        finalizeFile(file, finalPath);

        user.setProfilePicture(file);
        userRepository.save(user);
    }

    @Transactional // possibly move this to TopicController later
    public void saveTopicIcon(Long iconId, Long topicId) {
        File file = findFileById(iconId);
        // Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new RuntimeException("Topic not found"));
        Topic topic = new Topic(); // temporary code. delete later

        String finalPath = fileStorageService.moveToFinalStorage(file, FilePurpose.ICON);
        finalizeFile(file, finalPath);
        topic.setIcon(file);
        // topicRepository.save(topic);
        // add topic repository save later
    }

    private void finalizeFile(File file, String finalPath) {
        file.setFilePath(finalPath);
        file.activate();
        fileRepository.save(file);
    }

    private File findFileById(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File not found."));
    }
}
