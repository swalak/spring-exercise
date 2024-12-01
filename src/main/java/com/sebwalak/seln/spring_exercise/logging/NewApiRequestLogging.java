package com.sebwalak.seln.spring_exercise.logging;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

import static com.sebwalak.seln.spring_exercise.controller.SearchController.HEADER_API_KEY;

/**
 * This filter will control logging of messages handled by the newly created API (not proxy)
 */
public interface NewApiRequestLogging {
    static CommonsRequestLoggingFilter createLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setBeforeMessagePrefix(" BEGIN API MESSAGE: ");
        loggingFilter.setAfterMessagePrefix(" END API MESSAGE: ");
        loggingFilter.setHeaderPredicate(headerName -> !headerName.equalsIgnoreCase(HEADER_API_KEY));
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        return loggingFilter;
    }
}
