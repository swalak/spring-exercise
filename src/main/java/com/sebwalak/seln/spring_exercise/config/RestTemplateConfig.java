package com.sebwalak.seln.spring_exercise.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sebwalak.seln.spring_exercise.dump.ProxyResponseDumpingInterceptor;
import com.sebwalak.seln.spring_exercise.logging.ProxyMessageLoggingInterceptor;
import com.sebwalak.seln.spring_exercise.retry.HttpRequestRetryInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${spring.application.proxy.dump-requests-to-file}")
    private boolean dumpProxyRequestsToFile;

    @Autowired
    private HttpRequestRetryInterceptor httpRequestRetryInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        RestTemplateBuilder restTemplateBuilder = builder
                .messageConverters(messageConverter());

        // avoid chaining slow logging component if the logging is not requested
        if (ProxyMessageLoggingInterceptor.shouldBeEnabled()) {
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(new ProxyMessageLoggingInterceptor());
        }

        // avoid chaining slow disk writing component if the message dumping is not requested
        if (dumpProxyRequestsToFile) {
            restTemplateBuilder = restTemplateBuilder.additionalInterceptors(new ProxyResponseDumpingInterceptor());
        }

        return restTemplateBuilder
                .additionalInterceptors(httpRequestRetryInterceptor)
                .build();
    }

    public static AbstractGenericHttpMessageConverter<Object> messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);

        return messageConverter;
    }
}