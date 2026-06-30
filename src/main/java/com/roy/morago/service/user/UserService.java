package com.roy.morago.service.user;

import com.roy.morago.dto.socket.AdminActionEvent;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.dto.user.UserSearchRequest;
import com.roy.morago.dto.user.UserUpdateRequest;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.exception.user.MissingRoleException;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserMapper userMapper;
    private final UserHelper helper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return userMapper.toResponse(helper.findUserById(userId));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchRequest request) {
        Specification<User> spec = helper.buildSpecification(request);
        return userRepository.findAll(spec, request.toPageable())
                .map(userMapper::toResponse);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        log.info("Updating user: userId={}", id);
        User user = helper.findUserById(id);

        if (userUpdateRequest.languages() != null) {
            Set<Language> languages = languageRepository.findAllByNameIn(userUpdateRequest.languages());
            user.setLanguages(languages);
        }
        userMapper.toEntity(userUpdateRequest, user);
        log.info("User updated: userId={}", id);
    }

    @Transactional
    public void verifyTranslator(Long userId) {
        log.info("Verifying translator: userId={}", userId);
        User user = helper.findUserById(userId);
        boolean isTranslator = user.getRoles().stream()
                .anyMatch(role -> "ROLE_TRANSLATOR".equals(role.getName()));
        if (!isTranslator) {
            throw new MissingRoleException("User with ID " + userId + " is not a translator");
        }
        user.setStatus(UserStatus.VERIFIED);
        log.info("Translator verified: userId={}", userId);

        AdminActionEvent event = AdminActionEvent.from(user);
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: userId={}", id);
        User user = helper.findUserById(id);
        userRepository.delete(user);
        log.info("User deleted: userId={}, userName={}, email={}", id, user.getFullName(), user.getEmail());
    }
}
