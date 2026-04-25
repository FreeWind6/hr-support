package com.kubyshkin.hrsupport.config;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class TelegramClientConfig {

    private final TelegramProperties properties;

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public TelegramClient telegramClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        return new OkHttpTelegramClient(
                httpClient,
                properties.getToken(),
                new TelegramUrl(properties.getSchema(), properties.getHost(), properties.getPort(), false)
        );
    }
}
