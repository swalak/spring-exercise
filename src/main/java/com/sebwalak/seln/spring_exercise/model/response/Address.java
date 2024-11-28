package com.sebwalak.seln.spring_exercise.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Address(
        String premises,
        @JsonProperty("address_line_1") String addressLine1,
        String postalCode,
        String locality,
        String country
) {
}
