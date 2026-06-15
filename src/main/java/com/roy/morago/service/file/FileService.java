package com.roy.morago.service.file;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.entity.file.File;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.mapper.FileMapper;
import com.roy.morago.repository.file.FileRepository;
import com.roy.morago.repository.topic.TopicRepository;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.service.topic.TopicHelper;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;
    private final UserHelper userHelper;
    private final UserRepository userRepository;
    private final FileHelper fileHelper;
    private final TopicHelper topicHelper;
    private final TopicRepository topicRepository;

    @Transactional
    public FileResponse uploadProfilePicture(MultipartFile file) {
        return fileHelper.uploadFile(file, FilePurpose.PICTURE);
    }

    @Transactional
    public FileResponse uploadTopicIcon(MultipartFile file) {
        return fileHelper.uploadFile(file, FilePurpose.ICON);
    }

    @Transactional(readOnly = true)
    public FileResponse viewFile(Long fileId) {
        return fileMapper.createResponseFromEntity(fileHelper.findFileById(fileId));
    }

    @Transactional
    public void saveProfilePicture(Long userId, Long pictureId) {
        User user = userHelper.findUserById(userId);
        File file = fileHelper.findFileById(pictureId);

        String finalPath = fileStorageService.moveToFinalStorage(file, FilePurpose.PICTURE);
        fileHelper.finalizeFile(file, finalPath);

        user.setProfilePicture(file);
        userRepository.save(user);
    }

    @Transactional
    public void saveTopicIcon(Long topicId, Long iconId) {
        File icon = fileHelper.findFileById(iconId);
        Topic topic = topicHelper.findTopicById(topicId);

        String finalPath = fileStorageService.moveToFinalStorage(icon, FilePurpose.ICON);
        fileHelper.finalizeFile(icon, finalPath);
        topic.setIcon(icon);
        topicRepository.save(topic);
    }

    @Transactional
    public void deleteProfilePicture(Long userId) {
        User user = userHelper.findUserById(userId);
        File picture = fileHelper.findPictureByUser(user);
        user.setProfilePicture(null);
        fileRepository.deleteById(picture.getId());
        fileStorageService.deleteFromStorage(picture.getFilePath());
    }

    @Transactional
    public void deleteTopicIcon(Long topicId) {
        Topic topic = topicHelper.findTopicById(topicId);
        File icon = fileHelper.findIconByTopic(topic);
        topic.setIcon(null);
        fileRepository.deleteById(icon.getId());
        fileStorageService.deleteFromStorage(icon.getFilePath());
    }
}
