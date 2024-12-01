package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.sebwalak.seln.spring_exercise.model.request.SearchRequest;
import com.sebwalak.seln.spring_exercise.proxy.DataSource;
import com.sebwalak.seln.spring_exercise.service.SearchService;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

import static com.sebwalak.seln.spring_exercise.WireMockTestUtil.MOCKED_COMPANY_NUMBER_SERVICE_UNAVAILABLE;
import static com.sebwalak.seln.spring_exercise.WireMockTestUtil.VALID_API_KEY;
import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_API_KEY;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Tag("controller")
@Tag("service")
@Tag("integration")
@Tag("life-like-data")
@TestPropertySource(properties = {
        "spring.application.proxy.retry.back-off-delay-ms=500"
})
public class ProxyRequestRetryTest {

    @LocalServerPort
    private int localServerPort;

    @TestConfiguration
    public static class PrincipleTestConfiguration {
        @Value("${spring.application.proxy.base-url}")
        private String proxyBaseUrl;
        @Value("${spring.application.proxy.context-path}")
        private String proxyContextPath;
        @Autowired
        private RestTemplate restTemplate;
        @Autowired
        private DataSource dataSource;

        @Bean
        public SearchService searchService(@Autowired WireMockServer wireMockServer) {
            String testProxyBaseUrl = format("%s:%d%s/v1", proxyBaseUrl, wireMockServer.port(), proxyContextPath);

            return new SearchService(
                    dataSource.createFetchCompaniesFromProxy(restTemplate, testProxyBaseUrl),
                    dataSource.createFetchOfficersFromProxy(restTemplate, testProxyBaseUrl));
        }

        @Bean
        public WireMockServer wireMockServer() {
            return WireMockTestUtil.setUp(proxyContextPath, false);
        }
    }

    @Value("${spring.application.controller.endpoint-base-path}")
    private String endpointBasePath;
    @Value("${spring.application.controller.endpoint-name}")
    private String endpointName;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Tag("slow-test")
    void shouldRetryOnServiceUnavailable() {

        Instant startTime = now();

        //@formatter:off
        given()
            .basePath(endpointBasePath)
            .port(localServerPort)
            .contentType(JSON)
            .body(new SearchRequest(null, MOCKED_COMPANY_NUMBER_SERVICE_UNAVAILABLE))
            .accept(JSON)
            .header(HEADER_API_KEY, VALID_API_KEY)
        .when()
            .post(endpointName)
        .then()
            .statusCode(500)
            .contentType(JSON)
            .body(containsString("Whoops only!"))
            .body(not(containsString("123456789001010101")));

            Duration actualExecutionTime = Duration.between(startTime, now());
            Duration expectedExecutionTimeWithExponentialBackOffStrategy =
                    Duration.ofMillis(500).multipliedBy(1 + 2 + 4);

            assertThat("Execution time was shorter than expected",
                    actualExecutionTime,
                    greaterThanOrEqualTo(expectedExecutionTimeWithExponentialBackOffStrategy));
    }

}
