package com.ccojocea.aanews.models.responses;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewsApiErrorTest {

    @Test
    public void fromErrorBody_readsCode() {
        String body = "{\"status\":\"error\",\"code\":\"apiKeyMissing\",\"message\":\"Your API key is missing.\"}";
        assertEquals(NewsApiError.API_KEY_MISSING, NewsApiError.fromErrorBody(body));
    }

    @Test
    public void fromErrorBody_rateLimited() {
        String body = "{\"status\":\"error\",\"code\":\"rateLimited\",\"message\":\"You have made too many requests.\"}";
        assertEquals(NewsApiError.RATE_LIMITED, NewsApiError.fromErrorBody(body));
    }

    @Test
    public void fromErrorBody_unknownCode() {
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromErrorBody("{\"status\":\"error\",\"code\":\"somethingNew\"}"));
    }

    @Test
    public void fromErrorBody_invalidOrEmptyBody() {
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromErrorBody(null));
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromErrorBody(""));
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromErrorBody("<html>Bad gateway</html>"));
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromErrorBody("{\"status\":\"error\"}"));
    }

    @Test
    public void fromCode_allDocumentedCodes() {
        assertEquals(NewsApiError.API_KEY_DISABLED, NewsApiError.fromCode("apiKeyDisabled"));
        assertEquals(NewsApiError.API_KEY_EXHAUSTED, NewsApiError.fromCode("apiKeyExhausted"));
        assertEquals(NewsApiError.API_KEY_INVALID, NewsApiError.fromCode("apiKeyInvalid"));
        assertEquals(NewsApiError.PARAMETER_INVALID, NewsApiError.fromCode("parameterInvalid"));
        assertEquals(NewsApiError.PARAMETERS_MISSING, NewsApiError.fromCode("parametersMissing"));
        assertEquals(NewsApiError.SOURCES_TOO_MANY, NewsApiError.fromCode("sourcesTooMany"));
        assertEquals(NewsApiError.SOURCE_DOES_NOT_EXIST, NewsApiError.fromCode("sourceDoesNotExist"));
        assertEquals(NewsApiError.MAXIMUM_RESULTS_REACHED, NewsApiError.fromCode("maximumResultsReached"));
        assertEquals(NewsApiError.UNEXPECTED_ERROR, NewsApiError.fromCode("unexpectedError"));
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromCode(null));
    }

    @Test
    public void fromHttpStatus() {
        assertEquals(NewsApiError.API_KEY_INVALID, NewsApiError.fromHttpStatus(401));
        assertEquals(NewsApiError.RATE_LIMITED, NewsApiError.fromHttpStatus(429));
        assertEquals(NewsApiError.UNEXPECTED_ERROR, NewsApiError.fromHttpStatus(500));
        assertEquals(NewsApiError.UNKNOWN, NewsApiError.fromHttpStatus(418));
    }

}
