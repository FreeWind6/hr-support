package com.kubyshkin.hrsupport.repository;

import com.kubyshkin.hrsupport.model.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
}
