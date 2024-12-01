package com.sebwalak.seln.spring_exercise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.List;

import static java.util.List.of;
import static org.springframework.http.HttpStatus.*;

@Configuration
public class RetryConfig {
    @Bean
    public List<HttpStatus> httpStatusCodesThatAreRetryable() {
        return of(
                REQUEST_TIMEOUT,
                INTERNAL_SERVER_ERROR,
                BAD_GATEWAY,
                GATEWAY_TIMEOUT
        );
    }

    @Bean
    public List<HttpStatus> httpStatusCodesThatAreRetryableAfter() {
        return of(
                TOO_MANY_REQUESTS,
                SERVICE_UNAVAILABLE
        );
    }
}
