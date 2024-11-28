package com.sebwalak.seln.spring_exercise.model.response;

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
}
