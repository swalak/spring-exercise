package com.sebwalak.seln.spring_exercise.model.proxy;

import java.util.List;

public record OfficersFromProxy(
    List<OfficerFromProxy> items
) {}
