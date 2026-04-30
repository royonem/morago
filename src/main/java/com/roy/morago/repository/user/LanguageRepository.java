package com.roy.morago.repository.user;

import com.roy.morago.entity.user.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    Set<Language> findAllByNameIn(Set<String> names);
}
