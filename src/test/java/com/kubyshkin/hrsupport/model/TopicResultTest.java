package com.kubyshkin.hrsupport.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicResultTest {

    @Test
    void topicId_returnsCorrectValue() {
        TopicResult result = new TopicResult(42, false);
        assertThat(result.topicId()).isEqualTo(42);
    }

    @Test
    void isNew_returnsTrue_whenCreatedWithTrue() {
        TopicResult result = new TopicResult(1, true);
        assertThat(result.isNew()).isTrue();
    }

    @Test
    void isNew_returnsFalse_whenCreatedWithFalse() {
        TopicResult result = new TopicResult(1, false);
        assertThat(result.isNew()).isFalse();
    }

    @Test
    void equalRecords_areEqual() {
        TopicResult a = new TopicResult(10, true);
        TopicResult b = new TopicResult(10, true);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentRecords_areNotEqual() {
        TopicResult a = new TopicResult(10, true);
        TopicResult b = new TopicResult(20, false);
        assertThat(a).isNotEqualTo(b);
    }
}
