package com.api.framework.requests;

import com.api.framework.models.Resource;
import com.api.framework.utils.Constants;
import com.api.framework.utils.JsonFileReader;
import com.google.gson.Gson;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ResourceRequest class provides methods for performing CRUD operations on Resource entities.
 * <p>
 * This class extends {@link BaseRequest} to utilize common HTTP request methods.
 * </p>
 * Example usage:
 * {@code
 * ResourceRequest resourceRequest = new ResourceRequest();
 * Response response = resourceRequest.getResources();
 * }
 */
public class ResourceRequest extends BaseRequest {
    private String endpoint;

    /**
     * Fetches the list of all resources.
     *
     * @return A Response object containing the server's response to the GET request.
     */
    public Response getResources() {
        endpoint = String.format(Constants.URL, Constants.RESOURCES_PATH);
        return requestGet(endpoint, createBaseHeaders());
    }

    /**
     * Fetches a resource by its unique ID.
     *
     * @param resourceId The unique identifier of the resource.
     * @return A Response object containing the server's response to the GET request.
     */
    public Response getResource(String resourceId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.RESOURCES_PATH, resourceId);
        return requestGet(endpoint, createBaseHeaders());
    }

    /**
     * Creates a new resource.
     *
     * @param resource The resource object to be created.
     * @return A Response object containing the server's response to the POST request.
     */
    public Response createResource(Resource resource) {
        endpoint = String.format(Constants.URL, Constants.RESOURCES_PATH);
        return requestPost(endpoint, createBaseHeaders(), resource);
    }

    /**
     * Updates an existing resource by its unique ID.
     *
     * @param resource   The resource object containing updated information.
     * @param resourceId The unique identifier of the resource.
     * @return A Response object containing the server's response to the PUT request.
     */
    public Response updateResource(Resource resource, String resourceId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.RESOURCES_PATH, resourceId);
        return requestPut(endpoint, createBaseHeaders(), resource);
    }

    /**
     * Deletes a resource by its unique ID.
     *
     * @param resourceId The unique identifier of the resource.
     * @return A Response object containing the server's response to the DELETE request.
     */
    public Response deleteResource(String resourceId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.RESOURCES_PATH, resourceId);
        return requestDelete(endpoint, createBaseHeaders());
    }

    /**
     * Converts a Response object to a Resource entity.
     *
     * @param response The Response object containing the resource data.
     * @return A Resource object.
     */
    public Resource getResourceEntity(@NotNull Response response) {
        return response.as(Resource.class);
    }

    /**
     * Converts a Response object to a list of Resource entities.
     *
     * @param response The Response object containing the resource data.
     * @return A list of Resource objects.
     */
    public List<Resource> getResourcesEntity(@NotNull Response response) {
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("", Resource.class);
    }

    /**
     * Creates a default resource using data from a JSON file.
     *
     * @return A Response object containing the server's response to the POST request.
     */
    public Response createDefaultResource() {
        JsonFileReader jsonFile = new JsonFileReader();
        return this.createResource(jsonFile.getResourceByJson(Constants.DEFAULT_RESOURCE_FILE_PATH));
    }

    /**
     * Converts a JSON string to a Resource entity.
     *
     * @param resourceJson The JSON string representing a resource.
     * @return A Resource object.
     */
    public Resource getResourceEntity(String resourceJson) {
        Gson gson = new Gson();
        return gson.fromJson(resourceJson, Resource.class);
    }

    /**
     * Validates the JSON schema of a response.
     *
     * @param response   The Response object to be validated.
     * @param schemaPath The path to the JSON schema file.
     * @return True if the response matches the schema, false otherwise.
     */
    public boolean validateSchema(Response response, String schemaPath) {
        try {
            response.then()
                    .assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
            return true; // Return true if the assertion passes
        } catch (AssertionError e) {
            // Assertion failed, return false
            return false;
        }
    }
}