package com.sebwalak.seln.spring_exercise.retry;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class HttpRequestRetryInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private HttpRequestRetryQualifier httpRequestRetryQualifier;

    @Value("${spring.application.proxy.retry.back-off-delay-ms}")
    private long backOffDelayMs;

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        int maxAttempts = 3;
        int attempt = 1;
        long backoffDelay = backOffDelayMs; // initial backoff delay of 1 second

        ClientHttpResponse response;
        while (attempt <= maxAttempts) {
            try {
                response = execution.execute(request, body);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return response;
                } else if (!httpRequestRetryQualifier.isRetryable(response.getStatusCode())) {
                    return response;
                }

            } catch (IOException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
            }

            try {
                log.warn("Request will be retried after {} seconds. Retry attempt {} out of {}",
                        backoffDelay / 1000,
                        attempt,
                        maxAttempts);

                Thread.sleep(backoffDelay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted during backoff", ie);
            }
            attempt++;
            backoffDelay *= 2; // exponential backoff
        }
        throw new RuntimeException("Failed to execute request after " + maxAttempts + " attempts");
    }
}
