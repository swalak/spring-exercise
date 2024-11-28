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

    //language=JSON
    String validRequestOnlyCompanyName = """
            {
                "companyName" : "BBC LIMITED"
            }
            """;

    //language=JSON
    String validRequestOnlyCompanyNumber = """
            {
                "companyNumber" : "06500244"
            }
            """;

    String invalidRequestNoSearchValues = "{}";

    SearchRequest fromReadMeAsObject = new SearchRequest(
            "BBC LIMITED",
            "06500244"
    );
}
