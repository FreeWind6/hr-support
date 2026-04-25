package com.kubyshkin.hrsupport.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_topic")
@Getter
@Setter
@NoArgsConstructor
public class UserTopic {

    @Id
    private Long userId;

    private Integer topicId;

    public UserTopic(Long userId, Integer topicId) {
        this.userId = userId;
        this.topicId = topicId;
    }
}
