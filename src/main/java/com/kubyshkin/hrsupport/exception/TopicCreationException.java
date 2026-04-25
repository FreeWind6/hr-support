package com.kubyshkin.hrsupport.exception;

public class TopicCreationException extends RuntimeException {

    public TopicCreationException(long userId, Throwable cause) {
        super("Cannot create support topic for user " + userId, cause);
    }
}
