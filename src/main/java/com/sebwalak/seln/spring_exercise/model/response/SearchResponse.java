package com.sebwalak.seln.spring_exercise.model.response;

import java.util.List;

public record SearchResponse(
        int totalResults,
        List<Company> items) {
}
