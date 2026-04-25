package com.kubyshkin.hrsupport.repository;

import com.kubyshkin.hrsupport.model.UserTopic;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTopicRepository extends CrudRepository<UserTopic, UUID> {
    Optional<UserTopic> findByUserId(Long userId);
}
