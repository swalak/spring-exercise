package com.sebwalak.seln.spring_exercise.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class helps with retrieving Http response body.
 * Typical constructs require to read the body only once (it is a stream)
 * so if you read once in order to debug, when it comes to passing the body
 * to the client it won't be available anymore.
 *
 * This class retrieves it all and stores internally so that
 * it can be later retrieved multiple times.
 */
public class BufferedClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;
    private byte[] body;

    public BufferedClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
        this.response = response;
        this.body = response.getBody().readAllBytes();
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.body);
    }

    public String getBodyAsString() {
        return new String(this.body);
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }
}