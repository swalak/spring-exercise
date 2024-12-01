package com.sebwalak.seln.spring_exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebwalak.seln.spring_exercise.RequestExamples;
import com.sebwalak.seln.spring_exercise.ResponseExamples;
import com.sebwalak.seln.spring_exercise.model.request.SearchRequest;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;
import com.sebwalak.seln.spring_exercise.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_API_KEY;
import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_ONLY_ACTIVE;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc
@Tag("controller")
@Tag("unit")
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService mockedSearchService;

    @Value("${spring.application.controller.endpoint-base-path}")
    private String endpointBasePath;

    @Value("${spring.application.controller.endpoint-name}")
    private String endpointName;

    @Autowired
    private ObjectMapper objectMapper;

    final String expectedCompanyName = "anyCompanyName";
    final String expectedCompanyNumber = "anyCompanyNumber";
    final Boolean expectedOnlyActive = false;
    final String expectedApiKey = "anyApiKey";
    private URI controllerEndpointUri;

    @BeforeEach
    void setUp() {
        controllerEndpointUri = URI.create(format("%s%s", endpointBasePath, endpointName));
    }

    @Test
    void shouldHandleAMinimalValidRequest() throws Exception {

        // given
        final SearchResponse expectedSearchResponse = new SearchResponse(0, null);

        when(mockedSearchService.search(any(), any(), anyBoolean(), any())).thenReturn(expectedSearchResponse);

        final SearchRequest validSearchRequest = new SearchRequest(expectedCompanyName, expectedCompanyNumber);

        //@formatter:off
        // when
        mockMvc.perform(post(controllerEndpointUri)
                                .contentType(APPLICATION_JSON)
                                .header(HEADER_API_KEY, expectedApiKey)
                                .content(objectMapper.writeValueAsString(validSearchRequest)))

        // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedSearchResponse)));
        //@formatter:on

        verify(mockedSearchService).search(expectedCompanyName, expectedCompanyNumber, expectedOnlyActive, expectedApiKey);
    }

    @Test
    void shouldSerializeAndDeserializeExamplesIncludingCamelAndSnakeCase() throws Exception {

        when(mockedSearchService.search(
                ArgumentMatchers.eq(RequestExamples.fromReadMeAsObject.companyName()),
                eq(RequestExamples.fromReadMeAsObject.companyNumber()),
                anyBoolean(),
                anyString()
        )).thenReturn(ResponseExamples.fromReadMeAsObject);

        mockMvc.perform(post(controllerEndpointUri)
                        .contentType(APPLICATION_JSON)
                        .header(HEADER_API_KEY, expectedApiKey)
                        .content(RequestExamples.validRequestAsCamelCaseJson))
                .andExpect(content().json(ResponseExamples.validResponseAsSnakeCaseJson));
    }

    @Test
    void shouldDefaultOnlyActiveToFalse() throws Exception {
        mockMvc.perform(post(controllerEndpointUri)
                .contentType(APPLICATION_JSON)
                .header(HEADER_API_KEY, expectedApiKey)
                .content(RequestExamples.validRequestJson));

        verify(mockedSearchService).search(anyString(), anyString(), eq(false), anyString());
    }

    @Test
    void shouldAllowToSetOnlyActiveToTrue() throws Exception {
        mockMvc.perform(post(controllerEndpointUri)
                .contentType(APPLICATION_JSON)
                .header(HEADER_API_KEY, expectedApiKey)
                .queryParam(HEADER_ONLY_ACTIVE, "true")
                .content(RequestExamples.validRequestJson));

        verify(mockedSearchService).search(anyString(), anyString(), eq(true), anyString());
    }

    @Test
    void shouldComplainAboutMissingApiKey() throws Exception {
        final Exception actualResolvedException = mockMvc.perform(post(controllerEndpointUri)
                        .contentType(APPLICATION_JSON)
                        .content(RequestExamples.validRequestJson))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert actualResolvedException != null;

        assertThat("Exception message must match",
                actualResolvedException.getMessage(),
                matchesPattern(format("Required request header '%s' .* is not present", HEADER_API_KEY)));
    }

    @Test
    void shouldFailIfCompanyNameAndCompanyNumberAreMissing() throws Exception {
        final Exception actualResolvedException = mockMvc.perform(post(controllerEndpointUri)
                        .contentType(APPLICATION_JSON)
                        .header(HEADER_API_KEY, expectedApiKey)
                        .content(RequestExamples.invalidRequestNoSearchValues))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert actualResolvedException != null;

        assertEquals("Exception message",
                "Missing Company Name and Company Number",
                actualResolvedException.getMessage()
        );
    }

    @Test
    void shouldPassIfOnlyCompanyNameIsProvided() throws Exception {
        mockMvc.perform(post(controllerEndpointUri)
                        .contentType(APPLICATION_JSON)
                        .header(HEADER_API_KEY, expectedApiKey)
                        .content(RequestExamples.validRequestOnlyCompanyName))
                .andExpect(status().isOk());

        verify(mockedSearchService).search(
                eq(RequestExamples.fromReadMeAsObject.companyName()),
                isNull(),
                anyBoolean(),
                anyString());

    }

    @Test
    void shouldPassIfOnlyCompanyNumberIsProvided() throws Exception {
        mockMvc.perform(post(controllerEndpointUri)
                        .contentType(APPLICATION_JSON)
                        .header(HEADER_API_KEY, expectedApiKey)
                        .content(RequestExamples.validRequestOnlyCompanyNumber))
                .andExpect(status().isOk());

        verify(mockedSearchService).search(
                isNull(),
                eq(RequestExamples.fromReadMeAsObject.companyNumber()),
                anyBoolean(),
                anyString());

    }

}
