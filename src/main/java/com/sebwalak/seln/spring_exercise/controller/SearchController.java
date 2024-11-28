package com.sebwalak.seln.spring_exercise.controller;

import com.sebwalak.seln.spring_exercise.exception.MissingApiKeyHeaderException;
import com.sebwalak.seln.spring_exercise.model.request.SearchRequest;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;
import com.sebwalak.seln.spring_exercise.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(
        value = "${spring.application.controller.endpoint-base-path}",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
)
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(path = "${spring.application.controller.endpoint-name}")
    public @ResponseBody SearchResponse search(
            @RequestBody SearchRequest searchRequest,
            @RequestParam(value = "only-active", defaultValue = "false") boolean onlyActive,
            @RequestHeader(value = "x-api-key", required = false) String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new MissingApiKeyHeaderException();
        }

        searchRequest.validate();

        return searchService.search(
                searchRequest.companyName(),
                searchRequest.companyNumber(),
                onlyActive,
                apiKey
        );
    }

}
