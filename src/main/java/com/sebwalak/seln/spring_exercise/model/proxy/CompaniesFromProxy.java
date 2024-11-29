package com.sebwalak.seln.spring_exercise.model.proxy;

import java.util.List;

public record CompaniesFromProxy(
        int totalResults,
        List<CompanyFromProxy> items
) {}
