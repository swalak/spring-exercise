package com.sebwalak.seln.spring_exercise.service;

import com.sebwalak.seln.spring_exercise.model.proxy.CompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.CompanyFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficerFromProxy;
import com.sebwalak.seln.spring_exercise.model.proxy.OfficersFromProxy;
import com.sebwalak.seln.spring_exercise.model.response.Company;
import com.sebwalak.seln.spring_exercise.model.response.Officer;
import com.sebwalak.seln.spring_exercise.model.response.SearchResponse;
import com.sebwalak.seln.spring_exercise.proxy.FetchCompaniesFromProxy;
import com.sebwalak.seln.spring_exercise.proxy.FetchOfficersFromProxy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final FetchCompaniesFromProxy fetchCompaniesFromProxy;
    private final FetchOfficersFromProxy fetchOfficersFromProxy;

    public SearchService(
            FetchCompaniesFromProxy fetchCompaniesFromProxy,
            FetchOfficersFromProxy fetchOfficersFromProxy) {
        this.fetchCompaniesFromProxy = fetchCompaniesFromProxy;
        this.fetchOfficersFromProxy = fetchOfficersFromProxy;
    }

    public SearchResponse search(String companyName, String companyNumber, boolean onlyActive, String apiKey) {

        String searchQuery;
        if (isMissing(companyNumber)) {
            searchQuery = companyName;
        } else {
            searchQuery = companyNumber;
        }

        CompaniesFromProxy companiesFromProxy = fetchCompaniesFromProxy.by(searchQuery, apiKey);

        int totalResults = 0;
        List<Company> companies = new ArrayList<>();
        for (CompanyFromProxy companyFromProxy : companiesFromProxy.items()) {
            if (isCompanyActiveOrUserWantsAll(onlyActive, companyFromProxy)) {

                String officersCompanyNumber = companyFromProxy.companyNumber();

                OfficersFromProxy officersFromProxy = fetchOfficersFromProxy.by(officersCompanyNumber, apiKey);
                List<Officer> officers = officersFromProxy.items().stream()
                        .filter(SearchService::officerNotResigned)
                        .map(Officer::from)
                        .toList();

                Company company = Company.from(companyFromProxy, officers);
                companies.add(company);
                totalResults ++;
            }
        }

        return new SearchResponse(totalResults, companies);
    }

    private static boolean officerNotResigned(OfficerFromProxy officerFromProxy) {
        return officerFromProxy.resignedOn() == null;
    }

    private static boolean isCompanyActiveOrUserWantsAll(boolean onlyActive, CompanyFromProxy companyFromProxy) {
        return !onlyActive || companyFromProxy.companyStatus().equals("active");
    }

    private static boolean isMissing(String companyNumber) {
        return companyNumber == null || companyNumber.isEmpty();
    }

}
