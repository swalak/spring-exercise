package com.sebwalak.seln.spring_exercise.model.response;

import com.sebwalak.seln.spring_exercise.model.proxy.OfficerFromProxy;

public record Officer(
        String name,
        String officerRole,
        String appointedOn,
        Address address
) {

    public static Officer from(OfficerFromProxy o) {
        return new Officer(
                o.name(),
                o.officerRole(),
                o.appointedOn(),
                Address.from(o.address())
        );
    }
}
