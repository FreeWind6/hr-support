package com.kubyshkin.hrsupport.service.impl;

import com.kubyshkin.hrsupport.config.TelegramProperties;
import com.kubyshkin.hrsupport.model.TopicResult;
import com.kubyshkin.hrsupport.model.UserTopic;
import com.kubyshkin.hrsupport.repository.UserTopicRepository;
import com.kubyshkin.hrsupport.service.SupportTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.forum.CreateForumTopic;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportTopicServiceImpl implements SupportTopicService {

    private final UserTopicRepository userTopicRepository;
    private final TelegramClient telegramClient;
    private final TelegramProperties properties;

    /**
     * Возвращает результат поиска/создания темы в группе поддержки для данного пользователя.
     * {@link TopicResult#isNew()} == true означает, что тема была только что создана.
     */
    @Override
    public TopicResult findOrCreateTopic(long userId, String displayName) {
        return userTopicRepository.findById(userId)
                .map(ut -> new TopicResult(ut.getTopicId(), false))
                .orElseGet(() -> new TopicResult(createTopic(userId, displayName), true));
    }

    private int createTopic(long userId, String displayName) {
        String topicName = (displayName != null && !displayName.isBlank())
                ? displayName
                : "User " + userId;
        try {
            var forumTopic = telegramClient.execute(
                    CreateForumTopic.builder()
                            .chatId(properties.getSupportGroupId())
                            .name(topicName)
                            .build()
            );
            int topicId = forumTopic.getMessageThreadId();
            userTopicRepository.save(new UserTopic(userId, topicId));
            log.info("Created forum topic {} for user {} ({})", topicId, userId, topicName);
            return topicId;
        } catch (TelegramApiException e) {
            log.error("Failed to create forum topic for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Cannot create support topic for user " + userId, e);
        }
    }
}
