package com.automation.api.support;

import io.restassured.response.Response;

/**
 * State shared between step classes within one scenario.
 *
 * <p>Cucumber builds a fresh instance of every step class per scenario, so a {@code When} in
 * one class cannot hand its response to a {@code Then} in another. PicoContainer sees this
 * type in both constructors and passes the same instance to each — which is also what makes
 * the state scenario-scoped rather than static, so scenarios cannot leak into one another.
 */
public class TestContext {

    private Response response;

    public Response getResponse() {
        if (response == null) {
            throw new IllegalStateException(
                    "No response recorded yet. A Then step is asserting before any When step has called the API.");
        }
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
