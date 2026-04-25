package com.kubyshkin.hrsupport.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("user_topic")
@Getter
@NoArgsConstructor
public class UserTopic {

    @Id
    private UUID id;

    @Column("user_id")
    private Long userId;

    @Column("topic_id")
    private Integer topicId;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public UserTopic(Long userId, Integer topicId) {
        this.userId = userId;
        this.topicId = topicId;
    }
}
