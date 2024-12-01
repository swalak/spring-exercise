package com.sebwalak.seln.spring_exercise.controller;

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

    public static final String HEADER_ONLY_ACTIVE = "only-active";
    public static final String HEADER_API_KEY = "x-api-key";

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(path = "${spring.application.controller.endpoint-name}")
    public @ResponseBody SearchResponse search(
            @RequestBody SearchRequest searchRequest,
            @RequestParam(value = HEADER_ONLY_ACTIVE, defaultValue = "false") boolean onlyActive,
            @RequestHeader(value = HEADER_API_KEY) String apiKey) {

        searchRequest.validate();

        return searchService.search(
                searchRequest.companyName(),
                searchRequest.companyNumber(),
                onlyActive,
                apiKey
        );
    }

}
