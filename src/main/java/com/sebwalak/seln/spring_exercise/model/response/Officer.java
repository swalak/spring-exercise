package com.sebwalak.seln.spring_exercise.model.response;

public record Officer(
        String name,
        String officerRole,
        String appointedOn,
        Address address
) {
}
