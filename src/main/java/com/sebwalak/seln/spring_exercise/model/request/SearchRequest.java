package com.sebwalak.seln.spring_exercise.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sebwalak.seln.spring_exercise.exception.ValidationException;

public record SearchRequest(
        @JsonProperty("companyName") String companyName,
        @JsonProperty("companyNumber") String companyNumber) {

    public void validate() {
        if ((companyName == null || companyName.isBlank())
            && (companyNumber == null || companyNumber.isBlank())) {
            throw new ValidationException("Missing Company Name and Company Number");
        }
    }
}
