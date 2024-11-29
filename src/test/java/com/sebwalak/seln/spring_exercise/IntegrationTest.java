package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.sebwalak.seln.spring_exercise.model.request.SearchRequest;
import com.sebwalak.seln.spring_exercise.service.SearchService;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static com.sebwalak.seln.spring_exercise.WireMockTestUtil.*;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchCompaniesFromProxy;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchOfficersFromProxy;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class IntegrationTest {

    @LocalServerPort
    private int localServerPort;

    @TestConfiguration
    public static class PrincipleTestConfiguration {
        @Value("${spring.application.proxy.base-url}") private String proxyBaseUrl;
        @Value("${spring.application.proxy.context-path}") private String proxyContextPath;
        @Autowired private RestTemplate restTemplate;

        @Bean
        public SearchService searchService(@Autowired WireMockServer wireMockServer) {
            String testProxyBaseUrl = format("%s:%d%s/v1", proxyBaseUrl, wireMockServer.port(), proxyContextPath);

            return new SearchService(
                    createFetchCompaniesFromProxy(restTemplate, testProxyBaseUrl),
                    createFetchOfficersFromProxy(restTemplate, testProxyBaseUrl));
        }

        @Bean
        public WireMockServer wireMockServer() {
            return WireMockTestUtil.setUp(proxyContextPath, false);
        }
    }

    @Value("${spring.application.controller.endpoint-base-path}") private String endpointBasePath;
    @Value("${spring.application.controller.endpoint-name}") private String endpointName;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void shouldHandleABasicRequestFromControllerToMockedProxy() throws Exception {

        JsonPath expectedJson = new JsonPath(new File("src/test/resources/__files/company_with_officers_result.json"));

        //@formatter:off
        given()
            .basePath(endpointBasePath)
            .port(localServerPort)
            .contentType(JSON)
            .body(new SearchRequest(MOCKED_COMPANY_NAME, MOCKED_COMPANY_NUMBER))
            .accept(JSON)
            .header("x-api-key", VALID_API_KEY)
        .when()
            .post(endpointName)
        .then()
            .statusCode(200)
            .contentType(JSON)
			.body(not(equalTo(null)))
            .body("", equalTo(expectedJson.getMap("")));
        //@formatter:on
    }

}
