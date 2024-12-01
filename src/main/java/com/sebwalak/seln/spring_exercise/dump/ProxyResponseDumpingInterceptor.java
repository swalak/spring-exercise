package com.sebwalak.seln.spring_exercise.dump;

import com.sebwalak.seln.spring_exercise.web.BufferedClientHttpResponseWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Log4j2
public class ProxyResponseDumpingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] requestBody,
            ClientHttpRequestExecution execution) throws IOException {

        String query = request.getURI().getQuery();

        if (query == null) {
            // abandon dumping, unrecognised request
            return execution.execute(request, requestBody);
        }

        String typeDirectory;
        String searchTerm;
        if (query.startsWith("Query")) {
            typeDirectory = "search";
            searchTerm = query.substring(6);

        } else if (query.startsWith("CompanyNumber")) {
            typeDirectory = "officers";
            searchTerm = query.substring(14);
        } else {
            // abandon dumping, unrecognised request
            return execution.execute(request, requestBody);
        }

        ClientHttpResponse response = execution.execute(request, requestBody);
        BufferedClientHttpResponseWrapper responseWrapper = new BufferedClientHttpResponseWrapper(response);
        dumpResponse(typeDirectory, searchTerm, responseWrapper.getBodyAsString());

        return responseWrapper;
    }

    private void dumpResponse(String typeDirectory, String searchTerm, String body) throws IOException {
        String directoryPath = format("src/test/resources/__files/dump/proxy/%s/", typeDirectory);
        createDirectories(Paths.get(directoryPath));
        Path path = Paths.get(format("%s/%s.json", directoryPath, searchTerm));
        writeString(path, body, CREATE, TRUNCATE_EXISTING);
    }
}
