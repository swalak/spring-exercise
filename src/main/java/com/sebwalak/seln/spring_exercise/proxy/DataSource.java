package com.sebwalak.seln.spring_exercise.proxy;

import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_API_KEY;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.List.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriUtils.encode;

@Component
public class DataSource {

    public FetchCompaniesFromProxy createFetchCompaniesFromProxy(RestTemplate restTemplate, String proxyBaseUrl) {
        return (query, apiKey) -> {
            String url = format("%s/Search?Query=%s", proxyBaseUrl, encode(query, UTF_8));
            return send(url, restTemplate, apiKey, CompaniesFromProxy.class);
        };
    }

    public FetchOfficersFromProxy createFetchOfficersFromProxy(RestTemplate restTemplate, String proxyBaseUrl) {
        return (companyNumber, apiKey) -> {
            String url = format("%s/Officers?CompanyNumber=%s", proxyBaseUrl, encode(companyNumber, UTF_8));
            return send(url, restTemplate, apiKey, OfficersFromProxy.class);
        };
    }

    private <T> T send(String url, RestTemplate restTemplate, String apiKey, Class<T> responseType) {
        HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders(new MultiValueMapAdapter<>(Map.of(
                HEADER_API_KEY, of(apiKey),
                HttpHeaders.ACCEPT, of(APPLICATION_JSON_VALUE)
        ))));

        return restTemplate
                .exchange(url, GET, httpEntity, responseType)
                .getBody();
    }
}
