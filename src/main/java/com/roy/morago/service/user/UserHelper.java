package com.roy.morago.service.user;

import com.roy.morago.dto.user.UserSearchRequest;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.user.LanguageNotFoundException;
import com.roy.morago.exception.user.UserNotFoundException;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserHelper {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findUserWithAuthentication(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    protected Language findLanguageById(Long languageId) {
        return languageRepository.findById(languageId)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @SuppressWarnings("DataFlowIssue")
    protected Specification<User> buildSpecification(UserSearchRequest request) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = buildPredicates(request, root, cb);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicates(UserSearchRequest request, Root<User> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Optional.ofNullable(request.roleId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("role").get("id"), id)));
        Optional.ofNullable(request.topicId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("topic").get("id"), id)));
        Optional.ofNullable(request.languageId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("language").get("id"), id)));


        Optional.ofNullable(request.firstName()).ifPresent(name ->
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + name.toLowerCase() + "%")));
        Optional.ofNullable(request.lastName()).ifPresent(name ->
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + name.toLowerCase() + "%")));
        Optional.ofNullable(request.email()).ifPresent(email ->
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%")));
        Optional.ofNullable(request.phone()).ifPresent(phone ->
                predicates.add(cb.like(root.get("phone"), phone + "%")));
        Optional.ofNullable(request.status()).ifPresent(status ->
                predicates.add(cb.equal(root.get("status"), status)));
        Optional.ofNullable(request.availability()).ifPresent(availability ->
                predicates.add(cb.equal(root.get("availability"), availability)));

        // Topik range
        Optional.ofNullable(request.topikLevelFrom()).ifPresent(topik ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("topikLevel"), topik)));
        Optional.ofNullable(request.topikLevelTo()).ifPresent(topik ->
                predicates.add(cb.lessThanOrEqualTo(root.get("topikLevel"), topik)));

        // Birthdate range
        Optional.ofNullable(request.birthdateFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("birthDate"), date)));
        Optional.ofNullable(request.birthdateTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("birthDate"), date)));

        // Created date range
        Optional.ofNullable(request.createdFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), date)));
        Optional.ofNullable(request.createdTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), date)));
        return predicates;
    }
}