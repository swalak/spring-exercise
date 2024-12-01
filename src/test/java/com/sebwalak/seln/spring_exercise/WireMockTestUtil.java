package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WireMockTestUtil {

    public static final String VALID_API_KEY = "validApiKey";
    public final static String MOCKED_LIFELIKE_COMPANY_NUMBER = "06500244";
    public final static String MOCKED_LIFELIKE_COMPANY_NAME = "BBC LIMITED";
    public final static String MOCKED_COMPANY_NUMBER_NO_OFFICERS = "01481686";
    public final static String MOCKED_COMPANY_NAME = "BBC LIMITED";

    private static void stubCompanySearch(
            StringValuePattern queryPattern,
            String filenameWithResponseBody,
            String wireMockContextPath,
            WireMockServer wireMockServer) {

        System.out.println("query=" + queryPattern.getName() + " " + queryPattern.getValue() + " filename=" + filenameWithResponseBody);
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

        System.out.println("query=" + companyNumberPattern.getName() + " " + companyNumberPattern.getValue() + " filename=" + filenameWithResponseBody);

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

    public static String encode(String raw) {
        return UriUtils.encode(raw, UTF_8);
    }

    public static WireMockServer setUp(String wireMockContextPath, boolean verbose) {
        WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort();
        if (verbose) {
            wireMockConfiguration = wireMockConfiguration.notifier(new ConsoleNotifier(true));
        }
        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);

        // scanning `basePath` location for files and for each found file using
        // directory and file name structure to create appropriate stubs
        Path basePath = Paths.get("src/test/resources/__files");

        Consumer<Path> createProxyCompanySearchStub = path -> {
            String fileNameNoExt = path.getFileName().toString();
            fileNameNoExt = fileNameNoExt.substring(0, fileNameNoExt.lastIndexOf('.'));

            stubCompanySearch(
                    equalTo(fileNameNoExt),
                    basePath.relativize(path).toString(),
                    wireMockContextPath,
                    wireMockServer);

        };

        Consumer<Path> createProxyOfficersSearchStub = path -> {
            String fileNameNoExt = path.getFileName().toString();
            fileNameNoExt = fileNameNoExt.substring(0, fileNameNoExt.lastIndexOf('.'));

            stubOfficerSearch(
                    equalTo(fileNameNoExt),
                    basePath.relativize(path).toString(),
                    wireMockContextPath,
                    wireMockServer);
        };

        Stream.of("src/test/resources/__files/life-like/proxy/search",
                        "src/test/resources/__files/minimal/proxy/search")
                .map(File::new)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(File::isFile)
                .map(File::toPath)
                .forEach(createProxyCompanySearchStub);

        Stream.of("src/test/resources/__files/life-like/proxy/officers",
                        "src/test/resources/__files/minimal/proxy/officers")
                .map(File::new)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(File::isFile)
                .map(File::toPath)
                .forEach(createProxyOfficersSearchStub);

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
