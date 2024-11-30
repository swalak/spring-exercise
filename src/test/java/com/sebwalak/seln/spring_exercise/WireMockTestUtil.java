package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriUtils.encode;

public class WireMockTestUtil {

    public static final String VALID_API_KEY = "validApiKey";
    public final static String MOCKED_COMPANY_NUMBER = "06500244";
    public final static String MOCKED_COMPANY_NUMBER_2 = "065002440";
    public final static String MOCKED_COMPANY_NUMBER_NO_OFFICERS = "01481686";
    public final static String MOCKED_COMPANY_NAME = "BBC LIMITED";
    public final static String URL_ENCODED_MOCKED_COMPANY_NAME = encode(MOCKED_COMPANY_NAME, UTF_8);

    private static void stubCompanySearch(
            StringValuePattern queryPattern,
            String filenameWithResponseBody,
            String wireMockContextPath,
            WireMockServer wireMockServer) {

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Search", wireMockContextPath)))
                        .withQueryParam("Query", queryPattern)
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile(filenameWithResponseBody)));
    }

    private static void stubOfficerSearch(
            StringValuePattern companyNumberPattern,
            String filenameWithResponseBody,
            String wireMockContextPath,
            WireMockServer wireMockServer) {

        wireMockServer.stubFor(
                get(urlPathEqualTo(format("%s/v1/Officers", wireMockContextPath)))
                        .withQueryParam("CompanyNumber", companyNumberPattern)
                        .withHeader("Accept", equalTo(APPLICATION_JSON_VALUE))
                        .withHeader("x-api-key", equalTo(VALID_API_KEY))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBodyFile(filenameWithResponseBody)));
    }
    
    public static WireMockServer setUp(String wireMockContextPath, boolean verbose) {
        WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort();
        if (verbose) {
            wireMockConfiguration = wireMockConfiguration.notifier(new ConsoleNotifier(true));
        }
        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);

        stubCompanySearch(equalTo(MOCKED_COMPANY_NUMBER), "company_search_result_for_06500244.json", wireMockContextPath, wireMockServer);
        stubCompanySearch(equalTo(MOCKED_COMPANY_NUMBER_2), "company_search_result_for_065002440.json", wireMockContextPath, wireMockServer);
        stubCompanySearch(matching(URL_ENCODED_MOCKED_COMPANY_NAME), "company_search_result_for_BBC_LIMITED.json", wireMockContextPath, wireMockServer);
        stubOfficerSearch(equalTo(MOCKED_COMPANY_NUMBER), "officers_search_result_for_06500244.json", wireMockContextPath, wireMockServer);
        stubOfficerSearch(equalTo(MOCKED_COMPANY_NUMBER_2), "officers_search_result_for_065002440.json", wireMockContextPath, wireMockServer);
        stubOfficerSearch(equalTo(MOCKED_COMPANY_NUMBER_NO_OFFICERS), "officers_search_result_for_01481686_no_officers.json", wireMockContextPath, wireMockServer);

        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());
        
        return wireMockServer;
    }

    public static void tearDown(WireMockServer wireMockServer) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
