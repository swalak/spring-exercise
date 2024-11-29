package com.sebwalak.seln.spring_exercise.model.proxy;

import com.sebwalak.seln.spring_exercise.model.response.Address;

public record CompanyFromProxy(
        String companyNumber,
        String companyType,
        String title,
        String companyStatus,
        String dateOfCreation,
        Address address) {
}
