package com.sebwalak.seln.spring_exercise;

import com.sebwalak.seln.spring_exercise.model.request.SearchRequest;

public interface RequestExamples {

    //language=JSON
    String fromReadMeAsJson = """
            {
                "companyName" : "BBC LIMITED",
                "companyNumber" : "06500244"
            }
            """;

    String validRequestJson = fromReadMeAsJson;
    String validRequestAsCamelCaseJson = validRequestJson;

    SearchRequest fromReadMeAsObject = new SearchRequest(
            "BBC LIMITED",
            "06500244"
    );
}
