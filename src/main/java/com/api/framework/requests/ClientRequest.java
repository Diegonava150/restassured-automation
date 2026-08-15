package com.api.framework.requests;

import com.api.framework.models.Client;
import com.api.framework.utils.Constants;
import com.google.gson.Gson;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import java.util.List;

/** CRUD against PostgREST's {@code /clients} endpoint. */
public class ClientRequest extends BaseRequest {

    /**
     * Bodies are serialised with Gson explicitly rather than handing RestAssured an object.
     *
     * <p>RestAssured picks a mapper from whatever is on the classpath, and the two candidates
     * disagree on the thing that matters here: Gson omits null fields, Jackson writes them.
     * A created client has a null {@code id}, and {@code "id": null} makes Postgres reject the
     * insert outright. Choosing the mapper here makes that independent of the dependency tree.
     */
    private static final Gson GSON = new Gson();

    public Response getClients() {
        return requestGet(Constants.CLIENTS_PATH);
    }

    public Response getClient(Object clientId) {
        return requestGet(Constants.BY_ID.formatted(Constants.CLIENTS_PATH, clientId));
    }

    public Response createClient(Client client) {
        return requestPostRaw(Constants.CLIENTS_PATH, GSON.toJson(client));
    }

    /** Sends a body verbatim, so negative scenarios can post something no model could build. */
    public Response createClientRaw(String rawJson) {
        return requestPostRaw(Constants.CLIENTS_PATH, rawJson);
    }

    public Response updateClient(String rawJson, Object clientId) {
        return requestPatch(Constants.BY_ID.formatted(Constants.CLIENTS_PATH, clientId), rawJson);
    }

    public Response deleteClient(Object clientId) {
        return requestDelete(Constants.BY_ID.formatted(Constants.CLIENTS_PATH, clientId));
    }

    /**
     * The first client in the response.
     *
     * <p>PostgREST answers every read with an array, even a filter that can match at most one
     * row, so a single-object read is always "the array, then element zero".
     */
    public Client firstClient(Response response) {
        List<Client> clients = clientList(response);
        if (clients.isEmpty()) {
            throw new AssertionError("Expected at least one client in the response, but it was empty: "
                    + response.asString());
        }
        return clients.get(0);
    }

    public List<Client> clientList(Response response) {
        return response.jsonPath().getList("", Client.class);
    }

    public Client clientFromJson(String clientJson) {
        return GSON.fromJson(clientJson, Client.class);
    }

    /**
     * Asserts the response matches a JSON schema, and lets the failure through.
     *
     * <p>This used to return a boolean, catching the {@link AssertionError} and turning it
     * into {@code false} for an {@code assertTrue} to trip over. The validation was real, but
     * every failure reported "expected true, was false" and discarded the one thing worth
     * reading — which field, and what was wrong with it. Throwing is the whole point of an
     * assertion.
     */
    public void assertMatchesSchema(Response response, String schemaPath) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }
}
