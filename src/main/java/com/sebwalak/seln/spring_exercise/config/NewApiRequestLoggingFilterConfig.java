package com.sebwalak.seln.spring_exercise.config;

import com.sebwalak.seln.spring_exercise.logging.NewApiRequestLogging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class NewApiRequestLoggingFilterConfig {

    /**
     * This bean controls logging of messages handled by the newly created API (not proxy)
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        return NewApiRequestLogging.createLoggingFilter();
    }
}
