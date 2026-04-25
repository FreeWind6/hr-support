package com.kubyshkin.hrsupport.service.impl;

import com.kubyshkin.hrsupport.config.TelegramProperties;
import com.kubyshkin.hrsupport.model.TopicResult;
import com.kubyshkin.hrsupport.service.SupportTopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelegramBotServiceTest {

    private static final long SUPPORT_GROUP_ID = -1001234567890L;
    private static final long USER_ID = 555L;
    private static final long USER_CHAT_ID = 555L;
    private static final int TOPIC_ID = 999;
    private static final String BOT_TOKEN = "test-bot-token";
    private static final String WELCOME_TEXT = "Welcome!";

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private TelegramProperties properties;

    @Mock
    private SupportTopicService supportTopicService;

    @InjectMocks
    private TelegramBotService botService;

    @BeforeEach
    void setUp() {
        when(properties.getSupportGroupId()).thenReturn(SUPPORT_GROUP_ID);
        when(properties.getToken()).thenReturn(BOT_TOKEN);
        when(properties.getWelcomeText()).thenReturn(WELCOME_TEXT);
    }

    // ── getBotToken / getUpdatesConsumer ──────────────────────────────────────

    @Test
    void getBotToken_returnsTokenFromProperties() {
        assertThat(botService.getBotToken()).isEqualTo(BOT_TOKEN);
    }

    @Test
    void getUpdatesConsumer_returnsSelf() {
        LongPollingUpdateConsumer consumer = botService.getUpdatesConsumer();
        assertThat(consumer).isSameAs(botService);
    }

    // ── init ──────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void init_executesSetMyCommands() throws TelegramApiException {
        botService.init();
        verify(telegramClient).execute(any(SetMyCommands.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void init_telegramApiException_isCaughtSilently() throws TelegramApiException {
        when(telegramClient.execute(any(SetMyCommands.class))).thenThrow(new TelegramApiException("error"));
        botService.init();
        // no exception expected
    }

    // ── consume: guard conditions ─────────────────────────────────────────────

    @Test
    void consume_updateWithoutMessage_doesNothing() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);

        botService.consume(update);

        verifyNoInteractions(telegramClient, supportTopicService);
    }

    @Test
    void consume_messageFromSupportGroup_isIgnored() throws TelegramApiException {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(SUPPORT_GROUP_ID);

        botService.consume(update);

        verifyNoInteractions(supportTopicService);
        verify(telegramClient, never()).execute(any(ForwardMessage.class));
    }

    @Test
    void consume_messageWithUnsupportedType_isIgnored() throws TelegramApiException {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(USER_CHAT_ID);
        when(message.hasText()).thenReturn(false);
        when(message.hasPhoto()).thenReturn(false);
        when(message.hasVideo()).thenReturn(false);
        when(message.hasDocument()).thenReturn(false);
        when(message.hasAudio()).thenReturn(false);
        when(message.hasVoice()).thenReturn(false);
        when(message.hasVideoNote()).thenReturn(false);
        when(message.hasSticker()).thenReturn(false);

        botService.consume(update);

        verifyNoInteractions(supportTopicService);
        verify(telegramClient, never()).execute(any(ForwardMessage.class));
    }

    // ── consume: /start ───────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void consume_startCommand_sendsWelcomeMessage() throws TelegramApiException {
        botService.consume(buildTextUpdate("/start"));

        verify(telegramClient).execute((BotApiMethod<Serializable>) argThat(arg ->
                arg instanceof SendMessage sm && WELCOME_TEXT.equals(sm.getText())
        ));
        verifyNoInteractions(supportTopicService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void consume_startCommand_telegramApiException_caughtSilently() throws TelegramApiException {
        when(telegramClient.execute(any(SendMessage.class))).thenThrow(new TelegramApiException("send error"));

        botService.consume(buildTextUpdate("/start"));
        // no exception expected
    }

    // ── consume: forward ──────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void consume_textMessage_existingTopic_onlyForwardsMessage() throws TelegramApiException {
        when(supportTopicService.findOrCreateTopic(USER_ID, "Ivan"))
                .thenReturn(new TopicResult(TOPIC_ID, false));

        botService.consume(buildTextUpdate("Hello"));

        verify(telegramClient).execute(any(ForwardMessage.class));
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void consume_textMessage_newTopic_sendsUserLinkAndForwards() throws TelegramApiException {
        when(supportTopicService.findOrCreateTopic(USER_ID, "Ivan"))
                .thenReturn(new TopicResult(TOPIC_ID, true));

        botService.consume(buildTextUpdate("Hello"));

        verify(telegramClient).execute(any(SendMessage.class));
        verify(telegramClient).execute(any(ForwardMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void consume_photoMessage_isForwarded() throws TelegramApiException {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User sender = mock(User.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(USER_CHAT_ID);
        when(message.getFrom()).thenReturn(sender);
        when(message.hasText()).thenReturn(false);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getMessageId()).thenReturn(1);
        when(sender.getId()).thenReturn(USER_ID);
        when(sender.getFirstName()).thenReturn("Ivan");
        when(supportTopicService.findOrCreateTopic(USER_ID, "Ivan"))
                .thenReturn(new TopicResult(TOPIC_ID, false));

        botService.consume(update);

        verify(telegramClient).execute(any(ForwardMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void consume_forwardFails_exceptionCaughtSilently() throws TelegramApiException {
        when(supportTopicService.findOrCreateTopic(USER_ID, "Ivan"))
                .thenReturn(new TopicResult(TOPIC_ID, false));
        when(telegramClient.execute(any(ForwardMessage.class))).thenThrow(new TelegramApiException("forward error"));

        botService.consume(buildTextUpdate("Hello"));
        // no exception expected
    }

    // ── consume: display name ─────────────────────────────────────────────────

    @Test
    void consume_displayName_firstAndLastName() throws TelegramApiException {
        Update update = buildTextUpdateWithSender("Ivan", "Ivanov", null);
        when(supportTopicService.findOrCreateTopic(USER_ID, "Ivan Ivanov"))
                .thenReturn(new TopicResult(TOPIC_ID, false));

        botService.consume(update);

        verify(supportTopicService).findOrCreateTopic(USER_ID, "Ivan Ivanov");
    }

    @Test
    void consume_displayName_usesUsername_whenFirstNameIsEmpty() throws TelegramApiException {
        Update update = buildTextUpdateWithSender("", null, "johndoe");
        when(supportTopicService.findOrCreateTopic(USER_ID, "@johndoe"))
                .thenReturn(new TopicResult(TOPIC_ID, false));

        botService.consume(update);

        verify(supportTopicService).findOrCreateTopic(USER_ID, "@johndoe");
    }

    @Test
    void consume_displayName_truncatedWhenTooLong() throws TelegramApiException {
        String longName = "A".repeat(130);
        String expectedName = "A".repeat(125) + "...";
        Update update = buildTextUpdateWithSender(longName, null, null);
        when(supportTopicService.findOrCreateTopic(USER_ID, expectedName))
                .thenReturn(new TopicResult(TOPIC_ID, false));

        botService.consume(update);

        verify(supportTopicService).findOrCreateTopic(USER_ID, expectedName);
    }

    @Test
    @SuppressWarnings("unchecked")
    void consume_htmlSpecialCharsInDisplayName_escapedInUserLink() throws TelegramApiException {
        Update update = buildTextUpdateWithSender("<Ivan>", "&Ivanov", null);
        when(supportTopicService.findOrCreateTopic(eq(USER_ID), any()))
                .thenReturn(new TopicResult(TOPIC_ID, true));

        botService.consume(update);

        verify(telegramClient).execute((BotApiMethod<Serializable>) argThat(arg ->
                arg instanceof SendMessage sm
                        && sm.getText().contains("&lt;Ivan&gt;")
                        && sm.getText().contains("&amp;Ivanov")
        ));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Update buildTextUpdate(String text) {
        return buildTextUpdateWithSender("Ivan", null, null, text);
    }

    private Update buildTextUpdateWithSender(String firstName, String lastName, String userName) {
        return buildTextUpdateWithSender(firstName, lastName, userName, "Hello");
    }

    private Update buildTextUpdateWithSender(String firstName, String lastName, String userName, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User sender = mock(User.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(USER_CHAT_ID);
        when(message.getFrom()).thenReturn(sender);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getMessageId()).thenReturn(1);
        when(sender.getId()).thenReturn(USER_ID);
        when(sender.getFirstName()).thenReturn(firstName);
        when(sender.getLastName()).thenReturn(lastName);
        when(sender.getUserName()).thenReturn(userName);

        return update;
    }
}
