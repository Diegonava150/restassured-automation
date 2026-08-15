package com.api.framework.requests;

import com.api.framework.utils.Constants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Thin RestAssured wrapper: the HTTP verbs, and nothing else.
 *
 * <p>No base URI is set here. {@code RestAssured.baseURI} is assigned once per scenario from
 * the container's mapped port, so every request here is relative by design.
 */
public class BaseRequest {

    /**
     * A request with the JSON content type set, and optionally PostgREST's
     * {@code Prefer: return=representation}.
     *
     * <p>That header is the difference between a {@code 201} with an empty body and a
     * {@code 201} carrying the row that was actually written. Asserting on what the server
     * stored rather than on what you sent it is most of the value of a creation test.
     */
    private RequestSpecification request(boolean returnRepresentation) {
        RequestSpecification spec = RestAssured.given().contentType(Constants.VALUE_CONTENT_TYPE);
        return returnRepresentation ? spec.header(Constants.PREFER, Constants.RETURN_REPRESENTATION) : spec;
    }

    protected Response requestGet(String endpoint) {
        return request(false).when().get(endpoint);
    }

    protected Response requestPost(String endpoint, Object body) {
        return request(true).body(body).when().post(endpoint);
    }

    /**
     * PostgREST updates with PATCH against a filter, not PUT against a path segment. PUT
     * exists but demands the full row including its key, which makes a partial update
     * impossible to express.
     */
    protected Response requestPatch(String endpoint, Object body) {
        return request(true).body(body).when().patch(endpoint);
    }

    protected Response requestDelete(String endpoint) {
        return request(false).when().delete(endpoint);
    }

    /** Posts a raw string body, so a test can send JSON that no model could produce. */
    protected Response requestPostRaw(String endpoint, String rawBody) {
        return request(true).body(rawBody).when().post(endpoint);
    }
}
