package com.kubyshkin.hrsupport.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicCreationExceptionTest {

    @Test
    void message_containsUserId() {
        TopicCreationException ex = new TopicCreationException(12345L, new RuntimeException("cause"));
        assertThat(ex.getMessage()).isEqualTo("Cannot create support topic for user 12345");
    }

    @Test
    void cause_isPreserved() {
        RuntimeException cause = new RuntimeException("original cause");
        TopicCreationException ex = new TopicCreationException(1L, cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
