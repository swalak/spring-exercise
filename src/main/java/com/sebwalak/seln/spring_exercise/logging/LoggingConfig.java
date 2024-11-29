package com.sebwalak.seln.spring_exercise.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfig {

    /**
     * This bean controls logging of messages handled by the newly created API (not proxy)
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setBeforeMessagePrefix(" BEGIN API MESSAGE: ");
        loggingFilter.setAfterMessagePrefix(" END API MESSAGE: ");
        loggingFilter.setHeaderPredicate(headerName -> !headerName.equalsIgnoreCase("x-api-key"));
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        return loggingFilter;
    }
}
