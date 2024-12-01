package com.sebwalak.seln.spring_exercise.config;

import com.sebwalak.seln.spring_exercise.proxy.DataSource;
import com.sebwalak.seln.spring_exercise.proxy.FetchCompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.proxy.FetchOfficersFromProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;

@Configuration
public class DataSourceConfig {

    @Value("${spring.application.proxy.base-url}")
    private String proxyBaseUrl;

    @Value("${spring.application.proxy.context-path}")
    private String proxyContextPath;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @Bean
    public FetchCompaniesFromProxy fetchCompaniesFromProxy() {
        return dataSource.createFetchCompaniesFromProxy(
                restTemplate,
                format("%s%s/v1", proxyBaseUrl, proxyContextPath));
    }

    @Bean
    public FetchOfficersFromProxy fetchOfficersFromProxy() {
        return dataSource.createFetchOfficersFromProxy(
                restTemplate,
                format("%s%s/v1", proxyBaseUrl, proxyContextPath));
    }

}
