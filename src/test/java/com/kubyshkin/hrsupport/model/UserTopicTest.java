package com.kubyshkin.hrsupport.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTopicTest {

    @Test
    void noArgConstructor_createsInstance() {
        UserTopic userTopic = new UserTopic();
        assertThat(userTopic).isNotNull();
    }

    @Test
    void parameterizedConstructor_setsUserIdAndTopicId() {
        UserTopic userTopic = new UserTopic(123L, 456);
        assertThat(userTopic.getUserId()).isEqualTo(123L);
        assertThat(userTopic.getTopicId()).isEqualTo(456);
    }

    @Test
    void setters_andGetters_workCorrectly() {
        UserTopic userTopic = new UserTopic();
        userTopic.setUserId(999L);
        userTopic.setTopicId(888);
        assertThat(userTopic.getUserId()).isEqualTo(999L);
        assertThat(userTopic.getTopicId()).isEqualTo(888);
    }
}
