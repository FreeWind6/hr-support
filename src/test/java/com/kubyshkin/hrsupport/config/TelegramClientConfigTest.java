package com.kubyshkin.hrsupport.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramClientConfigTest {

    @Mock
    private TelegramProperties properties;

    @InjectMocks
    private TelegramClientConfig config;

    @Test
    void taskScheduler_returnsNonNull() {
        TaskScheduler scheduler = config.taskScheduler();
        assertThat(scheduler).isNotNull();
    }

    @Test
    void telegramClient_returnsNonNull() {
        when(properties.getToken()).thenReturn("test-token");
        when(properties.getSchema()).thenReturn("https");
        when(properties.getHost()).thenReturn("api.telegram.org");
        when(properties.getPort()).thenReturn(443);

        TelegramClient client = config.telegramClient();
        assertThat(client).isNotNull();
    }
}
