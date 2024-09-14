package com.api.framework.requests;

import com.api.framework.utils.Constants;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * BaseRequest class provides common HTTP request methods using RestAssured.
 * <p>
 * This class includes methods for performing GET, POST, PUT, and DELETE requests.
 * </p>
 * Example usage:
 * {@code
 * BaseRequest request = new BaseRequest();
 * Map<String, String> headers = request.createBaseHeaders();
 * Response response = request.requestGet("https://api.example.com/resource", headers);
 * }
 */
public class BaseRequest {
    /**
     * Sends a GET request to the specified endpoint with the given headers.
     *
     * @param endpoint The API URL endpoint.
     * @param headers  A map of headers to include in the request.
     * @return A Response object containing the server's response to the GET request.
     */
    protected Response requestGet(String endpoint, Map<String, ?> headers) {
        return RestAssured.given()
                .contentType(Constants.VALUE_CONTENT_TYPE)
                .headers(headers)
                .when()
                .get(endpoint);
    }

    /**
     * Sends a POST request to the specified endpoint with the given headers and body.
     *
     * @param endpoint The API URL endpoint.
     * @param headers  A map of headers to include in the request.
     * @param body     The body of the request, typically a model object.
     * @return A Response object containing the server's response to the POST request.
     */
    protected Response requestPost(String endpoint, Map<String, ?> headers, Object body) {
        return RestAssured.given()
                .contentType(Constants.VALUE_CONTENT_TYPE)
                .headers(headers)
                .body(body)
                .when()
                .post(endpoint);
    }

    /**
     * Sends a PUT request to the specified endpoint with the given headers and body.
     *
     * @param endpoint The API URL endpoint.
     * @param headers  A map of headers to include in the request.
     * @param body     The body of the request, typically a model object.
     * @return A Response object containing the server's response to the PUT request.
     */
    protected Response requestPut(String endpoint, Map<String, ?> headers, Object body) {
        return RestAssured.given()
                .contentType(Constants.VALUE_CONTENT_TYPE)
                .headers(headers)
                .body(body)
                .when()
                .put(endpoint);
    }

    /**
     * Sends a DELETE request to the specified endpoint with the given headers.
     *
     * @param endpoint The API URL endpoint.
     * @param headers  A map of headers to include in the request.
     * @return A Response object containing the server's response to the DELETE request.
     */
    protected Response requestDelete(String endpoint, Map<String, ?> headers) {
        return RestAssured.given()
                .contentType(Constants.VALUE_CONTENT_TYPE)
                .headers(headers)
                .when()
                .delete(endpoint);
    }

    /**
     * Creates a default map of headers with content type set to a predefined value.
     *
     * @return A map containing default headers.
     */
    protected Map<String, String> createBaseHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, Constants.VALUE_CONTENT_TYPE);
        return headers;
    }
}