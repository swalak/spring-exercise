package com.sebwalak.seln.spring_exercise.proxy;

import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;

public interface FetchOfficersFromProxy {
    OfficersFromProxy by(String companyNumber, String apiKey);
}
