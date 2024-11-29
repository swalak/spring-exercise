package com.sebwalak.seln.spring_exercise.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sebwalak.seln.spring_exercise.logging.LoggingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);

        RestTemplateBuilder restTemplateBuilder = builder
                .messageConverters(messageConverter);

        // avoid chaining an extra and very slow logging component if the logging is not requested
        if (LoggingInterceptor.shouldBeEnabled()) {
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(new LoggingInterceptor());
        }

        return restTemplateBuilder.build();
    }
}