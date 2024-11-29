package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriUtils.encode;

public class WireMockTestUtil {

    public static final String VALID_API_KEY = "validApiKey";
    public final static String MOCKED_COMPANY_NUMBER = "06500244";
    public final static String MOCKED_COMPANY_NUMBER_2 = "065002440";
    public final static String MOCKED_COMPANY_NAME= "BBC LIMITED";
    public final static String URL_ENCODED_MOCKED_COMPANY_NAME = encode(MOCKED_COMPANY_NAME, UTF_8);

    public static WireMockServer setUp(String wireMockContextPath, boolean verbose) {
        WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort();
        if (verbose) {
            wireMockConfiguration = wireMockConfiguration.notifier(new ConsoleNotifier(true));
        }
        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Search", wireMockContextPath)))
                        .withQueryParam("Query", equalTo(MOCKED_COMPANY_NUMBER))
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile("company_search_result_for_06500244.json")));

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Search", wireMockContextPath)))
                        .withQueryParam("Query", equalTo(MOCKED_COMPANY_NUMBER_2))
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile("company_search_result_for_065002440.json")));

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Search", wireMockContextPath)))
                        .withQueryParam("Query", matching(URL_ENCODED_MOCKED_COMPANY_NAME))
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile("company_search_result_for_BBC_LIMITED.json")));

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Officers", wireMockContextPath)))
                        .withQueryParam("CompanyNumber", equalTo(MOCKED_COMPANY_NUMBER))
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile("officers_search_result_for_06500244.json")));

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Officers", wireMockContextPath)))
                        .withQueryParam("CompanyNumber", equalTo(MOCKED_COMPANY_NUMBER_2))
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile("officers_search_result_for_065002440.json")));

        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());

        // verify stubs are working
        //@formatter:off
        given()
            .basePath(format("%s/v1", wireMockContextPath))
            .port(wireMockServer.port())
            .accept(APPLICATION_JSON_VALUE)
            .header("x-api-key", VALID_API_KEY)
            .queryParam("Query", MOCKED_COMPANY_NUMBER)
        .when()
            .get("/Search")
        .then()
            .statusCode(200)
            .contentType(APPLICATION_JSON_VALUE);

        given()
            .basePath(format("%s/v1", wireMockContextPath))
            .port(wireMockServer.port())
            .accept(APPLICATION_JSON_VALUE)
            .header("x-api-key", VALID_API_KEY)
            .queryParam("CompanyNumber", MOCKED_COMPANY_NUMBER)
        .when()
            .get("/Officers")
        .then()
            .statusCode(200)
            .contentType(APPLICATION_JSON_VALUE);
        //@formatter:on

        return wireMockServer;
    }

    public static void tearDown(WireMockServer wireMockServer) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
