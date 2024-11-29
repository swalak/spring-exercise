package com.sebwalak.seln.spring_exercise.proxy;

import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;

public interface FetchCompaniesFromProxy {
    CompaniesFromProxy by(String query, String apiKey);
}
