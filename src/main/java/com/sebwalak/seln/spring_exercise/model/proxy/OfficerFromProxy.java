package com.sebwalak.seln.spring_exercise.model.proxy;

import com.sebwalak.seln.spring_exercise.model.response.Address;

public record OfficerFromProxy(
        String name,
        String officerRole,
        String appointedOn,
        Address address,
        String resignedOn
) {
}
