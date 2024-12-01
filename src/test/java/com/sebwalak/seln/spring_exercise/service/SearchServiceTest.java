package com.sebwalak.seln.spring_exercise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.sebwalak.seln.spring_exercise.WireMockTestUtil;
import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.CompanyFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficerFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;
import com.sebwalak.seln.spring_exercise.model.response.Address;
import com.sebwalak.seln.spring_exercise.model.response.Company;
import com.sebwalak.seln.spring_exercise.model.response.Officer;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;
import com.sebwalak.seln.spring_exercise.proxy.FetchCompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.proxy.FetchOfficersFromProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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

import static com.sebwalak.seln.spring_exercise.WireMockTestUtil.VALID_API_KEY;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchCompaniesFromProxy;
import static com.sebwalak.seln.spring_exercise.proxy.DataSource.createFetchOfficersFromProxy;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Tag("service")
@Tag("unit")
class SearchServiceTest {

    public static final String ANY = "anything";
    public static final Address ADDRESS = new Address(ANY, ANY, ANY, ANY, ANY);
    public static final CompanyFromProxy ACTIVE_COMPANY_FROM_PROXY = new CompanyFromProxy(ANY, ANY, ANY, "active", ANY, ADDRESS);
    public static final OfficerFromProxy RESIGNED_OFFICER_FROM_PROXY = new OfficerFromProxy(ANY, ANY, ANY, ADDRESS, "2024-01-01");
    public static final OfficerFromProxy ACTIVE_OFFICER_FROM_PROXY = new OfficerFromProxy(ANY, ANY, ANY, ADDRESS, null);
    public static final boolean ONLY_ACTIVE_COMPANIES = true;
    public static final boolean ACTIVE_AND_INACTIVE_COMPANIES = false;

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

        wireMockServer = WireMockTestUtil.setUp(proxyContextPath, ACTIVE_AND_INACTIVE_COMPANIES);

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
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
    }

    @Test
    void shouldUseCompanyNameIfThatIsTheOnlyGiven() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, null);

        searchService.search(
                DUMMY_COMPANY_NAME,
                null,
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NAME), eq(VALID_API_KEY));
    }

    @Test
    void shouldUseCompanyNumberOverCompanyNameIfBothAreGiven() {
        searchService = new SearchService(mockedFetchCompaniesFromProxy, null);

        searchService.search(
                DUMMY_COMPANY_NAME,
                DUMMY_COMPANY_NUMBER,
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(DUMMY_COMPANY_NUMBER), eq(VALID_API_KEY));
    }

    @Test
    void shouldReturnEmptyResponseIfNoCompaniesAreFound() {
        when(mockedFetchCompaniesFromProxy.by(anyString(), anyString()))
                .thenReturn(new CompaniesFromProxy(0, emptyList()));
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);

        SearchResponse actualSearchResponse = searchService.search(null, DUMMY_COMPANY_NUMBER, ACTIVE_AND_INACTIVE_COMPANIES, VALID_API_KEY);

        assertThat(actualSearchResponse.totalResults(), is(0));
        assertThat(actualSearchResponse.items(), is(emptyList()));
    }

    @Test
    void shouldPassTheApiKeyDownToBothProxyAccessMethods() {

        // given
        final String TEST_SPECIFIC_VALID_API_KEY = "test-specific-valid-api-key";
        final String COMPANY_NUMBER = "single-use-company-number";
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);
        CompanyFromProxy companyFromProxy = new CompanyFromProxy(COMPANY_NUMBER, ANY, ANY, ANY, ANY, ADDRESS);
        when(mockedFetchCompaniesFromProxy.by(eq(COMPANY_NUMBER), eq(TEST_SPECIFIC_VALID_API_KEY)))
                .thenReturn(new CompaniesFromProxy(1, of(companyFromProxy)));

        // when
        searchService.search(
                null,
                COMPANY_NUMBER,
                ACTIVE_AND_INACTIVE_COMPANIES,
                TEST_SPECIFIC_VALID_API_KEY
        );

        // then
        Mockito.verify(mockedFetchCompaniesFromProxy).by(eq(COMPANY_NUMBER), eq(TEST_SPECIFIC_VALID_API_KEY));
        Mockito.verify(mockedFetchOfficersFromProxy).by(eq(COMPANY_NUMBER), eq(TEST_SPECIFIC_VALID_API_KEY));
    }

    @Test
    void shouldNotIncludeOfficersWhoResigned() {

        // given
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);

        when(mockedFetchCompaniesFromProxy.by(any(), any())).thenReturn(
                new CompaniesFromProxy(1, of(ACTIVE_COMPANY_FROM_PROXY)));

        when(mockedFetchOfficersFromProxy.by(any(), any())).thenReturn(
                new OfficersFromProxy(of(RESIGNED_OFFICER_FROM_PROXY, ACTIVE_OFFICER_FROM_PROXY)));

        // when
        SearchResponse actualSearchResponse = searchService.search(ANY, ANY, ACTIVE_AND_INACTIVE_COMPANIES, ANY);

        // then
        assertThat(actualSearchResponse.items().getFirst().officers(),
                contains(Officer.from(ACTIVE_OFFICER_FROM_PROXY)));

    }

    @Test
    @Tag("life-like-data")
    void shouldAllowToExcludeCompaniesWithoutActiveStatus() {
        SearchResponse actualSearchResponseWithOnlyActiveCompanies = searchService.search(
                "AB",
                null,
                ONLY_ACTIVE_COMPANIES,
                VALID_API_KEY);

        SearchResponse actualSearchResponseWithAllCompanies = searchService.search(
                "AB",
                null,
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        assertTrue(actualSearchResponseWithOnlyActiveCompanies.items().stream().allMatch(Company::isActive),
                "The list of only active companies should not contain a non active company");

        assertTrue(actualSearchResponseWithAllCompanies.items().stream().anyMatch(Company::isInactive),
                "The list should contain at least one inactive company");

        assertThat("The list of all companies is equal or a superset containing all active companies",
                actualSearchResponseWithAllCompanies.items(),
                hasItems(actualSearchResponseWithOnlyActiveCompanies.items().toArray(new Company[0])));

        assertThat("The list of all companies is longer than the list of only active ones",
                actualSearchResponseWithAllCompanies.items(),
                hasSize(greaterThanOrEqualTo(actualSearchResponseWithOnlyActiveCompanies.items().size())));
    }

    @Test
    @Tag("life-like-data")
    void shouldMergeOneCompanyWithItsOfficers() throws IOException {

        SearchResponse actualSearchResponse = searchService.search(
                null,
                "43210001",
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        File file = new File("src/test/resources/__files/minimal/expected/43210001.json");
        SearchResponse expectedSearchResponse = objectMapper.readValue(file, SearchResponse.class);
        assertThat(actualSearchResponse, is(expectedSearchResponse));
    }

    @Test
    @Tag("life-like-data")
    void shouldMergeMultipleCompaniesWithTheirOfficers() throws IOException {

        SearchResponse actualSearchResponse = searchService.search(
                "AB",
                null,
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        File file = new File("src/test/resources/__files/minimal/expected/AB.json");
        SearchResponse expectedSearchResponse = objectMapper.readValue(file, SearchResponse.class);
        assertThat(actualSearchResponse, is(expectedSearchResponse));
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
    ///```
    /// One of the companies matching the query criteria is:
    /// - named "B.B.C. BATHROOMS LIMITED"
    /// - number 01481686
    /// - status liquidation
    ///
    /// This company has no officers and that causes the service to failover.
    @Test
    void shouldCopeWithOfficersResponseWithNoOfficers() {

        // given
        searchService = new SearchService(mockedFetchCompaniesFromProxy, mockedFetchOfficersFromProxy);

        when(mockedFetchCompaniesFromProxy.by(any(), any())).thenReturn(
                new CompaniesFromProxy(1, of(ACTIVE_COMPANY_FROM_PROXY)));

        when(mockedFetchOfficersFromProxy.by(any(), any())).thenReturn(
                new OfficersFromProxy(emptyList()));

        // when
        SearchResponse actualSearchResponse = searchService.search(
                ANY,
                null,
                ACTIVE_AND_INACTIVE_COMPANIES,
                VALID_API_KEY);

        // then
        assertThat(actualSearchResponse.items(), hasSize(1));
        assertThat(actualSearchResponse.items().getFirst().officers(), is(emptyList()));
    }
}