package com.api.framework.requests;

import com.api.framework.models.Resource;
import com.api.framework.utils.Constants;
import com.google.gson.Gson;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import java.util.List;

/** CRUD against PostgREST's {@code /resources} endpoint. See {@link ClientRequest} on Gson. */
public class ResourceRequest extends BaseRequest {

    private static final Gson GSON = new Gson();

    public Response getResources() {
        return requestGet(Constants.RESOURCES_PATH);
    }

    /** Active resources only — the filter runs in Postgres, not in the test. */
    public Response getActiveResources() {
        return requestGet(Constants.RESOURCES_PATH + "?active=is.true");
    }

    public Response getResource(Object resourceId) {
        return requestGet(Constants.BY_ID.formatted(Constants.RESOURCES_PATH, resourceId));
    }

    /** Highest id first, one row: the most recently created resource. */
    public Response getLastCreatedResource() {
        return requestGet(Constants.RESOURCES_PATH + "?order=id.desc&limit=1");
    }

    public Response createResource(Resource resource) {
        return requestPostRaw(Constants.RESOURCES_PATH, GSON.toJson(resource));
    }

    public Response createResourceRaw(String rawJson) {
        return requestPostRaw(Constants.RESOURCES_PATH, rawJson);
    }

    public Response updateResource(String rawJson, Object resourceId) {
        return requestPatch(Constants.BY_ID.formatted(Constants.RESOURCES_PATH, resourceId), rawJson);
    }

    /** One PATCH against a filter updates every matching row — no client-side loop. */
    public Response deactivateAllActiveResources() {
        return requestPatch(Constants.RESOURCES_PATH + "?active=is.true", "{\"active\": false}");
    }

    public Response deleteResource(Object resourceId) {
        return requestDelete(Constants.BY_ID.formatted(Constants.RESOURCES_PATH, resourceId));
    }

    public Resource firstResource(Response response) {
        List<Resource> resources = resourceList(response);
        if (resources.isEmpty()) {
            throw new AssertionError(
                    "Expected at least one resource in the response, but it was empty: " + response.asString());
        }
        return resources.get(0);
    }

    public List<Resource> resourceList(Response response) {
        return response.jsonPath().getList("", Resource.class);
    }

    /** See {@link ClientRequest#assertMatchesSchema} on why this throws rather than returns. */
    public void assertMatchesSchema(Response response, String schemaPath) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }
}
