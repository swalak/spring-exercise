package com.sebwalak.seln.spring_exercise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.sebwalak.seln.spring_exercise.WireMockTestUtil;
import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.CompanyFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;
import com.sebwalak.seln.spring_exercise.model.response.Address;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;
import com.sebwalak.seln.spring_exercise.proxy.FetchCompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.proxy.FetchOfficersFromProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.sebwalak.seln.spring_exercise.WireMockTestUtil.*;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchCompaniesFromProxy;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchOfficersFromProxy;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class SearchServiceTest {

    private SearchService searchService;

    private WireMockServer wireMockServer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.proxy.base-url}")
    private String proxyBaseUrl;

    @Value("${spring.application.proxy.context-path}")
    private String proxyContextPath;

    @Mock
    private FetchCompaniesFromProxy mockedFetchCompaniesFromProxy;

    @Mock
    private FetchOfficersFromProxy mockedFetchOfficersFromProxy;

    private final static String DUMMY_COMPANY_NUMBER = "123";
    private final static String DUMMY_COMPANY_NAME = "ABC";

    @BeforeEach
    void setUp() {
        when(mockedFetchCompaniesFromProxy.by(anyString(), anyString()))
                .thenReturn(new CompaniesFromProxy(0, emptyList()));

        when(mockedFetchOfficersFromProxy.by(anyString(), anyString()))
                .thenReturn(new OfficersFromProxy(emptyList()));

        wireMockServer = WireMockTestUtil.setUp(proxyContextPath, false);

        String testProxyBaseUrl = format("%s:%d%s/v1", proxyBaseUrl, wireMockServer.port(), proxyContextPath);
        searchService = new SearchService(
                createFetchCompaniesFromProxy(restTemplate, testProxyBaseUrl),
                createFetchOfficersFromProxy(restTemplate, testProxyBaseUrl));
    }

    @AfterEach
    void tearDown() {
        WireMockTestUtil.tearDown(wireMockServer);
    }

    @Test
    void shouldUseCompanyNumberIfGiven() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, null);

        searchService.search(
                null,
                DUMMY_COMPANY_NUMBER,
                false,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
    }

    @Test
    void shouldUseCompanyNameIfThatIsTheOnlyGiven() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, null);

        searchService.search(
                DUMMY_COMPANY_NAME,
                null,
                false,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NAME), eq(VALID_API_KEY));
    }

    @Test
    void shouldUseCompanyNumberOverCompanyNameIfBothAreGiven() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, null);

        searchService.search(
                DUMMY_COMPANY_NAME,
                DUMMY_COMPANY_NUMBER,
                false,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
    }

    @Test
    void shouldReturnEmptyResponseIfNoCompaniesAreFound() {
        when(mockedFetchCompaniesFromProxy.by(anyString(), anyString()))
                .thenReturn(new CompaniesFromProxy(0, emptyList()));
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);

        SearchResponse actualSearchResponse = searchService.search(null, DUMMY_COMPANY_NUMBER, false, VALID_API_KEY);

        assertThat(actualSearchResponse.totalResults(), is(0));
        assertThat(actualSearchResponse.items(), is(emptyList()));
    }

    @Test
    void shouldPassTheApiKeyDownToBothProxyAccessMethods() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);

        CompanyFromProxy companyFromProxy = new CompanyFromProxy(
                DUMMY_COMPANY_NUMBER,
                null,
                null,
                null,
                null,
                new Address(null, null, null,null, null));


        when(mockedFetchCompaniesFromProxy.by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY)))
                .thenReturn(new CompaniesFromProxy(0, List.of(companyFromProxy)));

        searchService.search(
                null,
                DUMMY_COMPANY_NUMBER,
                false,
                VALID_API_KEY
        );

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
        Mockito.verify(mockedFetchOfficersFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
    }

    @Test
    void shouldCombineTheMockedProxyCompaniesWithMockedProxyOfficers() throws IOException {
        SearchResponse actualSearchResponse = searchService.search(
                null,
                MOCKED_COMPANY_NUMBER,
                false,
                VALID_API_KEY);


        File file = new File("src/test/resources/__files/company_with_officers_result.json");
        SearchResponse expectedSearchResponse = objectMapper.readValue(file, SearchResponse.class);
        assertThat(actualSearchResponse, is(expectedSearchResponse));
    }

    @Test
    void shouldExcludeCompaniesWithoutActiveStatus() {
        SearchResponse actualSearchResponse = searchService.search(
                MOCKED_COMPANY_NAME,
                null,
                true,
                VALID_API_KEY);

        assertThat(actualSearchResponse.totalResults(), is(1));
    }

    @Test
    void shouldIncludeCompaniesWithoutActiveStatus() {
        SearchResponse actualSearchResponse = searchService.search(
                MOCKED_COMPANY_NAME,
                null,
                false,
                VALID_API_KEY);

        assertThat(actualSearchResponse.totalResults(), is(2));
    }

    @Test
    void shouldNotIncludeOfficersWhoResigned() {
        SearchResponse actualSearchResponse = searchService.search(
                MOCKED_COMPANY_NAME,
                null,
                true,
                VALID_API_KEY);

        assertThat(actualSearchResponse.totalResults(), is(1));
        assertThat(actualSearchResponse.items().getFirst().officers(), hasSize(2));
    }

    /// A problem occurred when I run
    /// ```shell
    /// curl \
    ///     -s \
    ///     -X POST \
    ///     -d '{"companyName":"BBC LIMITED"}' \
    ///     -H "Content-Type: application/json" \
    ///     -H 'x-api-key: xxxx' \
    ///     "http://localhost:8080/api/v1/search"
    /// ```
    /// One of the companies matching the query criteria is:
    /// - named "B.B.C. BATHROOMS LIMITED"
    /// - number 01481686
    /// - status liquidation
    ///
    /// This company has no officers and that causes the service to failover.
    @Test
    void shouldCopeWithOfficersResponseWithNoOfficers() {
        //FIXME
        fail("to be implemented");
    }
}