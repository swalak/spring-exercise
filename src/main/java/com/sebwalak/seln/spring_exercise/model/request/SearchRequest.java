package com.sebwalak.seln.spring_exercise.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchRequest(
        @JsonProperty("companyName") String companyName,
        @JsonProperty("companyNumber") String companyNumber) {
}
