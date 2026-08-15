package com.automation.api.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.api.models.Client;
import com.automation.api.requests.ClientRequest;
import com.automation.api.support.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

/** Steps for {@code /clients}. */
public class ClientSteps {

    private final ClientRequest clients = new ClientRequest();
    private final TestContext context;

    private Client pendingClient;

    public ClientSteps(TestContext context) {
        this.context = context;
    }

    // ------------------------------------------------------------------ given

    /**
     * A precondition, asserted rather than created.
     *
     * <p>The old version of this step created clients in a loop until the count was high
     * enough — against a shared mock, which is how that mock filled to its row cap. The
     * container is seeded from {@code init.sql} and starts in a known state, so a
     * precondition can simply be checked. A step named "there are at least N" should not be
     * the thing that makes it so.
     */
    @Given("there are at least {int} registered clients")
    public void thereAreAtLeastRegisteredClients(int minimum) {
        Response response = clients.getClients();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(clients.clientList(response))
                .as("seeded clients from db/init.sql")
                .hasSizeGreaterThanOrEqualTo(minimum);
        context.setResponse(response);
    }

    @Given("I have a client with the following details:")
    public void iHaveAClientWithTheFollowingDetails(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        pendingClient = Client.builder()
                .name(row.get("name"))
                .lastName(row.get("lastName"))
                .country(row.get("country"))
                .city(row.get("city"))
                .phone(row.get("phone"))
                .email(row.get("email"))
                .build();
    }

    // ------------------------------------------------------------------- when

    @When("I request the list of clients")
    public void iRequestTheListOfClients() {
        context.setResponse(clients.getClients());
    }

    @When("I request the client with id {string}")
    public void iRequestTheClientWithId(String id) {
        context.setResponse(clients.getClient(id));
    }

    @When("I create the client")
    public void iCreateTheClient() {
        context.setResponse(clients.createClient(pendingClient));
    }

    @When("I create a client with the body:")
    public void iCreateAClientWithTheBody(String rawJson) {
        context.setResponse(clients.createClientRaw(rawJson));
    }

    @When("I update the client with id {string} with the body:")
    public void iUpdateTheClientWithIdWithTheBody(String id, String rawJson) {
        context.setResponse(clients.updateClient(rawJson, id));
    }

    @When("I delete the client with id {string}")
    public void iDeleteTheClientWithId(String id) {
        context.setResponse(clients.deleteClient(id));
    }

    // ------------------------------------------------------------------- then

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expected) {
        assertThat(context.getResponse().statusCode())
                .as("response body was: %s", context.getResponse().asString())
                .isEqualTo(expected);
    }

    /**
     * Asserts the PostgREST/Postgres error code, not just the status.
     *
     * <p>A {@code 400} tells you the request was rejected; {@code 23514} tells you a CHECK
     * constraint rejected it. Without this, a test asserting 400 passes just as happily when
     * the payload is malformed for an entirely different reason than the one under test.
     */
    @Then("the error code is {string}")
    public void theErrorCodeIs(String expectedCode) {
        assertThat(context.getResponse().jsonPath().getString("code"))
                .as("error body was: %s", context.getResponse().asString())
                .isEqualTo(expectedCode);
    }

    @Then("the response contains {int} clients")
    public void theResponseContainsClients(int expected) {
        assertThat(clients.clientList(context.getResponse())).hasSize(expected);
    }

    @Then("the response is an empty list")
    public void theResponseIsAnEmptyList() {
        assertThat(context.getResponse().jsonPath().getList("")).isEmpty();
    }

    @Then("the client in the response has:")
    public void theClientInTheResponseHas(DataTable table) {
        Client actual = clients.firstClient(context.getResponse());
        Map<String, String> expected = table.asMaps().get(0);

        assertThat(actual.getName()).isEqualTo(expected.get("name"));
        assertThat(actual.getLastName()).isEqualTo(expected.get("lastName"));
        assertThat(actual.getCountry()).isEqualTo(expected.get("country"));
        assertThat(actual.getCity()).isEqualTo(expected.get("city"));
        assertThat(actual.getPhone()).isEqualTo(expected.get("phone"));
        assertThat(actual.getEmail()).isEqualTo(expected.get("email"));
    }

    /**
     * The server assigned an id. Worth its own step: it is the difference between "the API
     * echoed my payload back" and "the API stored a row".
     */
    @Then("the created client has a server-assigned id")
    public void theCreatedClientHasAServerAssignedId() {
        assertThat(clients.firstClient(context.getResponse()).getId()).isNotNull().isPositive();
    }

    @Then("the response matches the client schema")
    public void theResponseMatchesTheClientSchema() {
        clients.assertMatchesSchema(context.getResponse(), "schemas/clientListSchema.json");
    }

    @Then("the client with id {string} no longer exists")
    public void theClientWithIdNoLongerExists(String id) {
        Response check = clients.getClient(id);
        assertThat(check.statusCode()).isEqualTo(200);
        assertThat(check.jsonPath().getList("")).as("row should be gone after DELETE").isEmpty();
    }

    @Then("every client has a non-empty {string}")
    public void everyClientHasANonEmpty(String field) {
        List<String> values = context.getResponse().jsonPath().getList(field, String.class);
        assertThat(values).isNotEmpty().allSatisfy(v -> assertThat(v).isNotBlank());
    }
}
