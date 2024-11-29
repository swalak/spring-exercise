package com.sebwalak.seln.spring_exercise.proxy;

import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.List.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriUtils.encode;

@Configuration
public class DataSource {

    @Value("${spring.application.proxy.base-url}")
    private String proxyBaseUrl;

    @Value("${spring.application.proxy.context-path}")
    private String proxyContextPath;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public FetchCompaniesFromProxy fetchCompaniesFromProxy() {
        return createFetchCompaniesFromProxy(restTemplate, format("%s%s/v1", proxyBaseUrl, proxyContextPath));
    }

    public static FetchCompaniesFromProxy createFetchCompaniesFromProxy(RestTemplate restTemplate, String proxyBaseUrl) {
        return (query, apiKey) -> {
            String url = format("%s/Search?Query=%s", proxyBaseUrl, encode(query, UTF_8));
            return send(url, restTemplate, apiKey, CompaniesFromProxy.class);
        };
    }

    @Bean
    public FetchOfficersFromProxy fetchOfficersFromProxy() {
        return createFetchOfficersFromProxy(restTemplate, format("%s%s/v1", proxyBaseUrl, proxyContextPath));
    }

    public static FetchOfficersFromProxy createFetchOfficersFromProxy(RestTemplate restTemplate, String proxyBaseUrl) {
        return (companyNumber, apiKey) -> {
            String url = format("%s/Officers?CompanyNumber=%s", proxyBaseUrl, encode(companyNumber, UTF_8));
            return send(url, restTemplate, apiKey, OfficersFromProxy.class);
        };
    }

    private static <T> T send(String url, RestTemplate restTemplate, String apiKey, Class<T> responseType) {
        HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders(new MultiValueMapAdapter<>(Map.of(
                "x-api-key", of(apiKey),
                "Accept", of(APPLICATION_JSON_VALUE)
        ))));

        return restTemplate
                .exchange(url, GET, httpEntity, responseType)
                .getBody();
    }
}
