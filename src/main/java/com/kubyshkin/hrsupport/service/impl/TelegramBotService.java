package com.kubyshkin.hrsupport.service.impl;

import com.kubyshkin.hrsupport.config.TelegramProperties;
import com.kubyshkin.hrsupport.model.TopicResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final String START_COMMAND = "/start";

    private final TelegramClient telegramClient;
    private final TelegramProperties properties;
    private final SupportTopicServiceImpl supportTopicService;

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @PostConstruct
    public void init() {
        try {
            telegramClient.execute(new SetMyCommands(
                    List.of(new BotCommand(START_COMMAND, "Запуск")),
                    new BotCommandScopeDefault(),
                    null
            ));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        var message = update.getMessage();
        if (!message.hasText() && !message.hasPhoto() && !message.hasVideo()
                && !message.hasDocument() && !message.hasAudio() && !message.hasVoice()
                && !message.hasVideoNote() && !message.hasSticker()) {
            return;
        }
        User sender = message.getFrom();
        long userId = sender.getId();
        long chatId = message.getChatId();

        if (START_COMMAND.equals(message.getText())) {
            handleStart(chatId);
            return;
        }

        String displayName = buildDisplayName(sender);
        TopicResult topicResult = supportTopicService.findOrCreateTopic(userId, displayName);
        int topicId = topicResult.topicId();

        if (topicResult.isNew()) {
            sendUserLinkToTopic(topicId, userId, displayName);
        }

        try {
            telegramClient.execute(
                    ForwardMessage.builder()
                            .chatId(properties.getSupportGroupId())
                            .messageThreadId(topicId)
                            .fromChatId(chatId)
                            .messageId(message.getMessageId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Failed to forward message from user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Отправляет приветственное сообщение на команду /start.
     * Тема в группе поддержки при этом не создаётся.
     */
    private void handleStart(long chatId) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(properties.getWelcomeText())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Failed to send welcome message to chat {}: {}", chatId, e.getMessage());
        }
    }

    /**
     * Отправляет в только что созданную тему ссылку на пользователя,
     * чтобы из темы можно было сразу перейти к нему.
     */
    private void sendUserLinkToTopic(int topicId, long userId, String displayName) {
        String userLink = String.format("<a href=\"tg://user?id=%d\">%s</a>", userId,
                escapeHtml(displayName.isBlank() ? "User " + userId : displayName));
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(properties.getSupportGroupId())
                            .messageThreadId(topicId)
                            .text("👤 " + userLink)
                            .parseMode("HTML")
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Failed to send user link to topic {} for user {}: {}", topicId, userId, e.getMessage());
        }
    }

    private String buildDisplayName(User user) {
        StringBuilder name = new StringBuilder();
        name.append(user.getFirstName());
        if (user.getLastName() != null) {
            if (!name.isEmpty()) name.append(' ');
            name.append(user.getLastName());
        }
        if (name.isEmpty() && user.getUserName() != null) {
            name.append('@').append(user.getUserName());
        }
        return name.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
