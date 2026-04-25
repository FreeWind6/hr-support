package com.kubyshkin.hrsupport.service.impl;

import com.kubyshkin.hrsupport.config.TelegramProperties;
import com.kubyshkin.hrsupport.exception.TopicCreationException;
import com.kubyshkin.hrsupport.model.TopicResult;
import com.kubyshkin.hrsupport.model.UserTopic;
import com.kubyshkin.hrsupport.repository.UserTopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.forum.CreateForumTopic;
import org.telegram.telegrambots.meta.api.objects.forum.ForumTopic;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTopicServiceImplTest {

    private static final long USER_ID = 100L;
    private static final int TOPIC_ID = 200;
    private static final long SUPPORT_GROUP_ID = -1001234567890L;

    @Mock
    private UserTopicRepository userTopicRepository;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private TelegramProperties properties;

    @InjectMocks
    private SupportTopicServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getSupportGroupId()).thenReturn(SUPPORT_GROUP_ID);
    }

    @Test
    void findOrCreateTopic_existingTopic_returnsTopicWithIsNewFalse() {
        UserTopic existing = new UserTopic(USER_ID, TOPIC_ID);
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        TopicResult result = service.findOrCreateTopic(USER_ID, "Ivan");

        assertThat(result.topicId()).isEqualTo(TOPIC_ID);
        assertThat(result.isNew()).isFalse();
        verifyNoInteractions(telegramClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_noTopic_createsAndReturnsIsNewTrue() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);

        TopicResult result = service.findOrCreateTopic(USER_ID, "Ivan");

        assertThat(result.topicId()).isEqualTo(TOPIC_ID);
        assertThat(result.isNew()).isTrue();
        verify(userTopicRepository).save(any(UserTopic.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_nullDisplayName_usesUserIdAsTopicName() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);

        service.findOrCreateTopic(USER_ID, null);

        verify(telegramClient).execute((BotApiMethod<Serializable>) argThat(arg ->
                arg instanceof CreateForumTopic cft && ("User " + USER_ID).equals(cft.getName())
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_blankDisplayName_usesUserIdAsTopicName() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);

        service.findOrCreateTopic(USER_ID, "   ");

        verify(telegramClient).execute((BotApiMethod<Serializable>) argThat(arg ->
                arg instanceof CreateForumTopic cft && ("User " + USER_ID).equals(cft.getName())
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_validDisplayName_usesDisplayName() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);

        service.findOrCreateTopic(USER_ID, "Ivan Ivanov");

        verify(telegramClient).execute((BotApiMethod<Serializable>) argThat(arg ->
                arg instanceof CreateForumTopic cft && "Ivan Ivanov".equals(cft.getName())
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_telegramApiException_throwsTopicCreationException() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(telegramClient.execute(any(CreateForumTopic.class)))
                .thenThrow(new TelegramApiException("Telegram error"));

        assertThatThrownBy(() -> service.findOrCreateTopic(USER_ID, "Ivan"))
                .isInstanceOf(TopicCreationException.class)
                .hasMessageContaining(String.valueOf(USER_ID));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_dataIntegrityViolation_fetchesExistingTopic() throws TelegramApiException {
        UserTopic winner = new UserTopic(USER_ID, TOPIC_ID);
        when(userTopicRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);
        when(userTopicRepository.save(any(UserTopic.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        TopicResult result = service.findOrCreateTopic(USER_ID, "Ivan");

        assertThat(result.topicId()).isEqualTo(TOPIC_ID);
        assertThat(result.isNew()).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findOrCreateTopic_dataIntegrityViolation_secondFindReturnsEmpty_throwsException() throws TelegramApiException {
        when(userTopicRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        ForumTopic forumTopic = mock(ForumTopic.class);
        when(forumTopic.getMessageThreadId()).thenReturn(TOPIC_ID);
        when(telegramClient.execute(any(CreateForumTopic.class))).thenReturn(forumTopic);
        when(userTopicRepository.save(any(UserTopic.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.findOrCreateTopic(USER_ID, "Ivan"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
