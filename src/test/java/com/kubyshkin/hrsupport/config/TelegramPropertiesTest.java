package com.kubyshkin.hrsupport.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramPropertiesTest {

    @Test
    void gettersAndSetters_workCorrectly() {
        TelegramProperties props = new TelegramProperties();
        props.setSchema("https");
        props.setHost("api.telegram.org");
        props.setPort(443);
        props.setToken("bot-token-123");
        props.setSupportGroupId(-100123456L);
        props.setWelcomeText("Hello!");

        assertThat(props.getSchema()).isEqualTo("https");
        assertThat(props.getHost()).isEqualTo("api.telegram.org");
        assertThat(props.getPort()).isEqualTo(443);
        assertThat(props.getToken()).isEqualTo("bot-token-123");
        assertThat(props.getSupportGroupId()).isEqualTo(-100123456L);
        assertThat(props.getWelcomeText()).isEqualTo("Hello!");
    }
}
