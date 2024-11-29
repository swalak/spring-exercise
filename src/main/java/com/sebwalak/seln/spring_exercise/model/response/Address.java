package com.sebwalak.seln.spring_exercise.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Address(
        String premises,
        @JsonProperty("address_line_1") String addressLine1,
        String postalCode,
        String locality,
        String country
) {

    public static Address from(Address o) {
        return new Address(
                o.premises,
                o.addressLine1,
                o.postalCode,
                o.locality,
                o.country
        );
    }
}
