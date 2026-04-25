package com.kubyshkin.hrsupport.repository;

import com.kubyshkin.hrsupport.model.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTopicRepository extends JpaRepository<UserTopic, UUID> {
    Optional<UserTopic> findByUserId(Long userId);
}
