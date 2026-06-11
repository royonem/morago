package com.roy.morago.repository.topic;

import com.roy.morago.entity.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    boolean existsByNameIgnoreCase(String name);
}
