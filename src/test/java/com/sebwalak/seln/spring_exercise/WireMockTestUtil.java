package com.sebwalak.seln.spring_exercise;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_API_KEY;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

interface ProxyStubMaker {
    void createProxyStub(
            String pathElementName,
            String queryParameterName,
            StringValuePattern queryPattern,
            int responseCode,
            Path stubResponseBody);
}

@Log4j2
public class WireMockTestUtil {

    public static final String VALID_API_KEY = "validApiKey";
    public final static String MOCKED_LIFELIKE_COMPANY_NUMBER = "06500244";
    public final static String MOCKED_LIFELIKE_COMPANY_NAME = "BBC LIMITED";
    public final static String MOCKED_COMPANY_NUMBER_SERVER_ERROR = "43210010";
    public final static String MOCKED_COMPANY_NUMBER_SERVICE_UNAVAILABLE = "43210011";

    public static WireMockServer setUp(String wireMockContextPath, boolean verbose) {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .notifier(new ConsoleNotifier(verbose)));

        ProxyStubMaker proxyStubMaker = (pathElementName, queryParameterName, queryPattern, responseCode, stubResponseBody) ->
                wireMockServer.stubFor(
                        get(urlPathEqualTo(format("%s/v1/%s", wireMockContextPath, pathElementName)))
                                .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
                                .withHeader(HEADER_API_KEY, equalTo(VALID_API_KEY))
                                .withQueryParam(queryParameterName, queryPattern)
                                .willReturn(aResponse()
                                        .withStatus(responseCode)
                                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                        .withBodyFile(stubResponseBody.toString())
                                )
                );

        autoCreateStubsBasedOnDiscoveredJsonFiles(proxyStubMaker);

        proxyStubMaker.createProxyStub(
                "Search",
                "Query",
                equalTo(MOCKED_COMPANY_NUMBER_SERVER_ERROR),
                INTERNAL_SERVER_ERROR.value(),
                Path.of("canned-responses/proxy-does-whoops"));

        proxyStubMaker.createProxyStub(
                "Search",
                "Query",
                equalTo(MOCKED_COMPANY_NUMBER_SERVICE_UNAVAILABLE),
                SERVICE_UNAVAILABLE.value(),
                Path.of("canned-responses/service-unavailable")
        );

        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());

        return wireMockServer;
    }

    private static void autoCreateStubsBasedOnDiscoveredJsonFiles(ProxyStubMaker proxyStubMaker) {
        // scanning `basePath` location for files and for each found file using
        // directory and file name structure to create appropriate stubs
        Path basePath = Paths.get("src/test/resources/__files");

        Consumer<Path> pathToStub = path -> {
            String fileNameNoExt = getFilenameWithoutExtension(path.getFileName().toString());
            StringValuePattern queryParameterValueMatcher = equalTo(fileNameNoExt);
            Path pathWithResponseBody = basePath.relativize(path);

            log.debug("auto-stubbing query={}({}) => filename={}",
                    queryParameterValueMatcher.getName(), queryParameterValueMatcher.getValue(), pathWithResponseBody);

            if (path.toString().matches("^.*/proxy/search/.*[.]json$")) {
                proxyStubMaker.createProxyStub(
                        "Search",
                        "Query",
                        queryParameterValueMatcher,
                        200,
                        pathWithResponseBody);
            } else if (path.toString().matches("^.*/proxy/officers/.*[.]json$")) {
                proxyStubMaker.createProxyStub(
                        "Officers",
                        "CompanyNumber",
                        queryParameterValueMatcher,
                        200,
                        pathWithResponseBody);
            }
        };

        Stream.of("life-like/proxy/search",
                        "minimal/proxy/search",
                        "life-like/proxy/officers",
                        "minimal/proxy/officers")
                .map(basePath::resolve)                     // "prepend" the string with basePath
                .map(Path::toFile)
                .map(File::listFiles)                       // directory listing
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)                    // will flatten nested collections
                .filter(File::isFile)
                .map(File::toPath)
                .forEach(pathToStub);                       // analyse path and then crete WireMock stub
    }

    private static String getFilenameWithoutExtension(String fileNameNoExt) {
        fileNameNoExt = fileNameNoExt.substring(0, fileNameNoExt.lastIndexOf('.'));
        return fileNameNoExt;
    }

    public static void tearDown(WireMockServer wireMockServer) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
