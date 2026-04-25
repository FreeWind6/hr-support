package com.kubyshkin.hrsupport.service;

import com.kubyshkin.hrsupport.model.TopicResult;

public interface SupportTopicService {
    TopicResult findOrCreateTopic(long userId, String displayName);
}
