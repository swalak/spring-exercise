package com.sebwalak.seln.spring_exercise;

import com.sebwalak.seln.spring_exercise.model.response.Address;
import com.sebwalak.seln.spring_exercise.model.response.Company;
import com.sebwalak.seln.spring_exercise.model.response.Officer;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;

import java.util.List;

public interface ResponseExamples {

    //language=JSON
    String fromReadMeAsJson = """
            {
                "total_results": 1,
                "items": [
                    {
                        "company_number": "06500244",
                        "company_type": "ltd",
                        "title": "BBC LIMITED",
                        "company_status": "active",
                        "date_of_creation": "2008-02-11",
                        "address": {
                            "locality": "Retford",
                            "postal_code": "DN22 0AD",
                            "premises": "Boswell Cottage Main Street",
                            "address_line_1": "North Leverton",
                            "country": "England"
                        },
                        "officers": [
                            {
                                "name": "BOXALL, Sarah Victoria",
                                "officer_role": "secretary",
                                "appointed_on": "2008-02-11",
                                "address": {
                                    "premises": "5",
                                    "locality": "London",
                                    "address_line_1": "Cranford Close",
                                    "country": "England",
                                    "postal_code": "SW20 0DP"
                                }
                            }
                        ]
                    }
                ]
            }
            """;

    String validResponseJson = fromReadMeAsJson;
    String validResponseAsSnakeCaseJson = validResponseJson;

    SearchResponse fromReadMeAsObject = new SearchResponse(1, List.of(
            new Company(
                    "06500244",
                    "ltd",
                    "BBC LIMITED",
                    "active",
                    "2008-02-11",
                    new Address(
                            "Boswell Cottage Main Street",
                            "North Leverton",
                            "DN22 0AD",
                            "Retford",
                            "England"
                    ),
                    List.of(
                            new Officer(
                                    "BOXALL, Sarah Victoria",
                                    "secretary",
                                    "2008-02-11",
                                    new Address(
                                            "5",
                                            "Cranford Close",
                                            "SW20 0DP",
                                            "London",
                                            "England"
                                    )
                            )
                    )

            )
    ));
}
