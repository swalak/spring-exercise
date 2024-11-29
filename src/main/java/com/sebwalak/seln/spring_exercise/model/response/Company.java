package com.sebwalak.seln.spring_exercise.model.response;

import com.sebwalak.seln.spring_exercise.model.proxy.CompanyFromProxy;

import java.util.List;

public record Company(
        String companyNumber,
        String companyType,
        String title,
        String companyStatus,
        String dateOfCreation,
        Address address,
        List<Officer> officers
) {
    public static Company from(CompanyFromProxy o, List<Officer> withOfficers) {
        return new Company(
                o.companyNumber(),
                o.companyType(),
                o.title(),
                o.companyStatus(),
                o.dateOfCreation(),
                Address.from(o.address()),
                withOfficers
        );
    }
}
