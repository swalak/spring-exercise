package com.sebwalak.seln.spring_exercise.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebwalak.seln.spring_exercise.web.BufferedClientHttpResponseWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This interceptor is meant to be used for logging of messages handled by the proxy client (using RestTemplate)
 */
@Log4j2
public class ProxyMessageLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final ObjectMapper compactingObjectMapper = new ObjectMapper();

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] requestBody,
            ClientHttpRequestExecution execution) throws IOException {

        logRequest(request, new String(requestBody, UTF_8));

        ClientHttpResponse response = execution.execute(request, requestBody);

        BufferedClientHttpResponseWrapper responseWrapper = new BufferedClientHttpResponseWrapper(response);

        logResponse(response, responseWrapper.getBodyAsString());
        return responseWrapper;
    }

    public static boolean shouldBeEnabled() {
        return log.isDebugEnabled();
    }

    private void logRequest(HttpRequest request, String body) {
        log.debug("  BEGIN PROXY API REQUEST");
        log.debug("  URI: {}", request.getURI());
        log.debug("  Method: {}", request.getMethod());
        log.debug("  Headers: {}", request.getHeaders());
        log.debug("  Request Body: {}", compactJson(body));
        log.debug("  END PROXY API REQUEST");
    }

    private void logResponse(ClientHttpResponse response, String body) throws IOException {
        log.debug("  BEGIN PROXY API RESPONSE");
        log.debug("  Status Code: {}", response.getStatusCode());
        log.debug("  Status Text: {}", response.getStatusText());
        log.debug("  Headers: {}", response.getHeaders());
        log.debug("  Response Body: {}", compactJson(body));
        log.debug("  END PROXY API RESPONSE");
    }

    private String compactJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            body = compactingObjectMapper.writeValueAsString(compactingObjectMapper.readValue(body, Object.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse the message body as JSON, passing as is. Message: {}", e.getMessage());
        }
        return body;
    }
}