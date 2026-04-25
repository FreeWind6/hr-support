package com.kubyshkin.hrsupport.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration
@EnableJdbcAuditing
@Slf4j
@RequiredArgsConstructor
public class AppConfig {
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    @PostConstruct
    private void init() {
        log.info("App started, version: {}, commit_id: {}", buildProperties.getVersion(), gitProperties.getShortCommitId());
    }
}
