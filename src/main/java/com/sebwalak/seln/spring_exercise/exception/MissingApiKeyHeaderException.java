package com.sebwalak.seln.spring_exercise.exception;

public class MissingApiKeyHeaderException extends RuntimeException {

    public static final String MESSAGE = "Header 'x-api-key' needs to contain an API key";

    public MissingApiKeyHeaderException() {
        super(MESSAGE);
    }
}
