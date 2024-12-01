package com.sebwalak.seln.spring_exercise.retry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class HttpRequestRetryQualifier {

    public final List<HttpStatus> retryable;

    // Currently all status codes here trigger same reaction but the following ones should obey "Reply-After"
    public final List<HttpStatus> retryableAfter;

    @Autowired
    public HttpRequestRetryQualifier(
            List<HttpStatus> httpStatusCodesThatAreRetryable,
            List<HttpStatus> httpStatusCodesThatAreRetryableAfter) {
        this.retryable = httpStatusCodesThatAreRetryable;
        this.retryableAfter = httpStatusCodesThatAreRetryableAfter;
    }

    public boolean isRetryable(HttpStatusCode statusCode) {
        return Stream.concat(retryable.stream(), retryableAfter.stream())
                .anyMatch(statusCode::isSameCodeAs);
    }
}
