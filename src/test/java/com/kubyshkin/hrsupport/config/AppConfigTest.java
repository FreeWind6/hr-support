package com.kubyshkin.hrsupport.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Mock
    private BuildProperties buildProperties;

    @Mock
    private GitProperties gitProperties;

    @InjectMocks
    private AppConfig appConfig;

    @Test
    void init_doesNotThrow() {
        when(buildProperties.getVersion()).thenReturn("0.2.0");
        when(gitProperties.getShortCommitId()).thenReturn("abc1234");

        assertThatCode(() -> {
            Method initMethod = AppConfig.class.getDeclaredMethod("init");
            initMethod.setAccessible(true);
            initMethod.invoke(appConfig);
        }).doesNotThrowAnyException();
    }
}
