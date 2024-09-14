package com.testing.framework.stepDefinitions;

import com.api.framework.models.Client;
import com.api.framework.requests.ClientRequest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

/**
 * ClientSteps class contains step definitions for Cucumber scenarios related to Client operations.
 * <p>
 * This class uses {@link ClientRequest} to perform API requests and validate responses.
 * </p>
 */
public class ClientSteps {
    private static final Logger logger = LogManager.getLogger(ClientSteps.class);

    private final ClientRequest clientRequest = new ClientRequest();

    private Response response;
    private Client client;

    @Given("there are at least 10 registered clients in the system")
    public void thereAreAtLeast10RegisteredClientsInTheSystem() {
        response = clientRequest.getClients();
        logger.info(response.jsonPath().prettify());
        Assert.assertEquals(200, response.statusCode());

        List<Client> clientList = clientRequest.getClientsEntity(response);
        while (clientList.size() < 10) {
            response = clientRequest.createDefaultClient();
            logger.info(response.statusCode());
            Assert.assertEquals(201, response.statusCode());
            clientList = clientRequest.getClientsEntity(clientRequest.getClients());
        }
    }

    @Given("I have a client with the following details:")
    public void iHaveAClientWithTheFollowingDetails(DataTable clientData) {
        Map<String, String> clientDataMap = clientData.asMaps().get(0);
        client = Client.builder()
                .name(clientDataMap.get("Name"))
                .lastName(clientDataMap.get("LastName"))
                .country(clientDataMap.get("Country"))
                .city(clientDataMap.get("City"))
                .id(clientDataMap.get("Id"))
                .phone(clientDataMap.get("Phone"))
                .email(clientDataMap.get("Email"))
                .build();
        logger.info("Client mapped: " + client);
    }

    @When("I retrieve the details of the client with id {string}")
    public void sendGETRequestId(String clientId) {
        response = clientRequest.getClient(clientId);
        logger.info(response.jsonPath().prettify());
        logger.info("The status code is: " + response.statusCode());
    }

    @When("I retrieve the details of the client with name {string}")
    public void sendGETRequest(String clientName) {
        Response clientsResponse = clientRequest.getClients();

        if (clientsResponse.getStatusCode() != 200) {
            logger.error("Failed to fetch clients list");
            Assert.fail("Failed to fetch clients list with status code: " + clientsResponse.getStatusCode());
            return;
        }

        List<Client> clients = clientsResponse.jsonPath().getList("", Client.class);
        Client matchedClient = clients.stream()
                .filter(client -> clientName.equalsIgnoreCase(client.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client with name " + clientName + " not found"));

        String clientId = matchedClient.getId();
        response = clientRequest.getClient(clientId);

        logger.info("Response Body: " + response.jsonPath().prettify());
        logger.info("The status code is: " + response.statusCode());

        if (response.getStatusCode() == 200) {
            this.client = response.as(Client.class);
            logger.info("Client details retrieved: " + this.client);
        } else {
            logger.error("Failed to fetch details for client with ID: " + clientId);
        }
    }

    @When("I save her current phone number")
    public void iSaveHerCurrentPhoneNumber() {
        String originalPhoneNumber = client.getPhone();
    }

    @When("I send a PUT request to update the client with ID {string}")
    public void iSendAPUTRequestToUpdateTheClientWithID(String clientId, String requestBody) {
        client = clientRequest.getClientEntity(requestBody);
        response = clientRequest.updateClient(client, clientId);
    }

    @Then("the client should have a status code of {int}")
    public void theResponseShouldHaveAStatusCodeOf(int statusCode) {
        Assert.assertEquals(statusCode, response.statusCode());
    }

    @Then("the client should have the following details:")
    public void theResponseShouldHaveTheFollowingDetails(DataTable expectedData) {
        client = clientRequest.getClientEntity(response);
        Map<String, String> expectedDataMap = expectedData.asMaps().get(0);

        Assert.assertEquals(expectedDataMap.get("Name"), client.getName());
        Assert.assertEquals(expectedDataMap.get("LastName"), client.getLastName());
        Assert.assertEquals(expectedDataMap.get("Country"), client.getCountry());
        Assert.assertEquals(expectedDataMap.get("City"), client.getCity());
        Assert.assertEquals(expectedDataMap.get("Id"), client.getId());
        Assert.assertEquals(expectedDataMap.get("Phone"), client.getPhone());
        Assert.assertEquals(expectedDataMap.get("Email"), client.getEmail());
    }

    @Then("validates the response with client JSON schema")
    public void userValidatesResponseWithClientJSONSchema() {
        String path = "schemas/clientSchema.json";
        Assert.assertTrue(clientRequest.validateSchema(response, path));
        logger.info("Successfully validated schema for Client object");
    }

    @Then("I delete all the registered clients")
    public void iDeleteAllTheRegisteredClients() {
        response = clientRequest.getClients();
        List<Client> clients = clientRequest.getClientsEntity(response);

        for (Client cli : clients) {
            response = clientRequest.deleteClient(cli.getId());
            Assert.assertEquals(200, response.statusCode());
            logger.info("Deleted client with ID: " + cli.getId());
        }
    }


    @When("I send a GET request to view all the clients")
    public void iSendAGETRequestToViewAllTheClient() {
        response = clientRequest.getClients();
    }

    @When("I send a POST request to create a client")
    public void iSendAPOSTRequestToCreateAClient() {
        response = clientRequest.createClient(client);
    }

    @When("I send a DELETE request to delete the client with ID {string}")
    public void iSendADELETERequestToDeleteTheClientWithID(String clientId) {
        response = clientRequest.deleteClient(clientId);
    }

    @Then("the response should include the details of the created client")
    public void theResponseShouldIncludeTheDetailsOfTheCreatedClient() {
        Client newClient = clientRequest.getClientEntity(response);
        newClient.setId(null);
        Assert.assertEquals(client, newClient);
    }

    @Then("validates the response with client list JSON schema")
    public void userValidatesResponseWithClientListJSONSchema() {
        String path = "schemas/clientListSchema.json";
        Assert.assertTrue(clientRequest.validateSchema(response, path));
        logger.info("Successfully validated schema for Client List object");
    }
}