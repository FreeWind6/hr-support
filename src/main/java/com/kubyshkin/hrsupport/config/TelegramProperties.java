package com.kubyshkin.hrsupport.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "telegram-bot")
@Data
@Validated
public class TelegramProperties {
    /**
     * Схема url подключения. http или https
     */
    @NotBlank
    private String schema;

    /**
     * Адрес подключения
     */
    @NotBlank
    private String host;

    /**
     * Порт подключения
     */
    @NotNull
    private Integer port;

    /**
     * Токен доступа
     */
    @NotBlank
    private String token;

    /**
     * ID группы поддержки (супергруппа с включёнными темами)
     */
    @NotNull
    private Long supportGroupId;

    /**
     * Приветственное сообщение на команду /start
     */
    @NotBlank
    private String welcomeText;
}
