package com.api.framework.requests;

import com.google.gson.Gson;
import com.api.framework.models.Client;
import com.api.framework.utils.Constants;
import com.api.framework.utils.JsonFileReader;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ClientRequest class provides methods for performing CRUD operations on Client resources.
 * <p>
 * This class extends {@link BaseRequest} to utilize common HTTP request methods.
 * </p>
 * Example usage:
 * {@code
 * ClientRequest clientRequest = new ClientRequest();
 * Response response = clientRequest.getClients();
 * }
 */
public class ClientRequest extends BaseRequest {
    private String endpoint;

    /**
     * Fetches the list of all clients.
     *
     * @return A Response object containing the server's response to the GET request.
     */
    public Response getClients() {
        endpoint = String.format(Constants.URL, Constants.CLIENTS_PATH);
        return requestGet(endpoint, createBaseHeaders());
    }

    /**
     * Fetches a client by its unique ID.
     *
     * @param clientId The unique identifier of the client.
     * @return A Response object containing the server's response to the GET request.
     */
    public Response getClient(String clientId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.CLIENTS_PATH, clientId);
        return requestGet(endpoint, createBaseHeaders());
    }

    /**
     * Fetches the name of a client by its unique ID.
     *
     * @param clientId The unique identifier of the client.
     * @return The name of the client.
     * @throws RuntimeException if the client details could not be fetched.
     */
    public String getClientName(String clientId) {
        Response response = getClient(clientId);
        if (response.getStatusCode() == 200) {
            Client client = response.as(Client.class);
            return client.getName();
        } else {
            throw new RuntimeException("Failed to fetch client details for ID: " + clientId);
        }
    }

    /**
     * Creates a new client.
     *
     * @param client The client object to be created.
     * @return A Response object containing the server's response to the POST request.
     */
    public Response createClient(Client client) {
        endpoint = String.format(Constants.URL, Constants.CLIENTS_PATH);
        return requestPost(endpoint, createBaseHeaders(), client);
    }

    /**
     * Updates an existing client by its unique ID.
     *
     * @param client   The client object containing updated information.
     * @param clientId The unique identifier of the client.
     * @return A Response object containing the server's response to the PUT request.
     */
    public Response updateClient(Client client, String clientId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.CLIENTS_PATH, clientId);
        return requestPut(endpoint, createBaseHeaders(), client);
    }

    /**
     * Deletes a client by its unique ID.
     *
     * @param clientId The unique identifier of the client.
     * @return A Response object containing the server's response to the DELETE request.
     */
    public Response deleteClient(String clientId) {
        endpoint = String.format(Constants.URL_WITH_PARAM, Constants.CLIENTS_PATH, clientId);
        return requestDelete(endpoint, createBaseHeaders());
    }

    /**
     * Converts a Response object to a Client entity.
     *
     * @param response The Response object containing the client data.
     * @return A Client object.
     */
    public Client getClientEntity(@NotNull Response response) {
        return response.as(Client.class);
    }

    /**
     * Converts a Response object to a list of Client entities.
     *
     * @param response The Response object containing the client data.
     * @return A list of Client objects.
     */
    public List<Client> getClientsEntity(@NotNull Response response) {
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("", Client.class);
    }

    /**
     * Creates a default client using data from a JSON file.
     *
     * @return A Response object containing the server's response to the POST request.
     */
    public Response createDefaultClient() {
        JsonFileReader jsonFile = new JsonFileReader();
        return this.createClient(jsonFile.getClientByJson(Constants.DEFAULT_CLIENT_FILE_PATH));
    }

    /**
     * Converts a JSON string to a Client entity.
     *
     * @param clientJson The JSON string representing a client.
     * @return A Client object.
     */
    public Client getClientEntity(String clientJson) {
        Gson gson = new Gson();
        return gson.fromJson(clientJson, Client.class);
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