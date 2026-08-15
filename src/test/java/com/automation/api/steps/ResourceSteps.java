package com.automation.api.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.api.models.Resource;
import com.automation.api.requests.ResourceRequest;
import com.automation.api.support.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;

/** Steps for {@code /resources}. */
public class ResourceSteps {

    private final ResourceRequest resources = new ResourceRequest();
    private final TestContext context;

    private Integer lastResourceId;

    public ResourceSteps(TestContext context) {
        this.context = context;
    }

    // ------------------------------------------------------------------ given

    @Given("there are at least {int} resources")
    public void thereAreAtLeastResources(int minimum) {
        Response response = resources.getResources();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(resources.resourceList(response))
                .as("seeded resources from db/init.sql")
                .hasSizeGreaterThanOrEqualTo(minimum);
        context.setResponse(response);
    }

    @Given("there are at least {int} active resources")
    public void thereAreAtLeastActiveResources(int minimum) {
        Response response = resources.getActiveResources();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(resources.resourceList(response)).hasSizeGreaterThanOrEqualTo(minimum);
        context.setResponse(response);
    }

    // ------------------------------------------------------------------- when

    @When("I request the list of resources")
    public void iRequestTheListOfResources() {
        context.setResponse(resources.getResources());
    }

    @When("I request the active resources")
    public void iRequestTheActiveResources() {
        context.setResponse(resources.getActiveResources());
    }

    @When("I request the most recently created resource")
    public void iRequestTheMostRecentlyCreatedResource() {
        Response response = resources.getLastCreatedResource();
        lastResourceId = resources.firstResource(response).getId();
        context.setResponse(response);
    }

    @When("I update that resource with:")
    public void iUpdateThatResourceWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        Resource update = Resource.builder()
                .name(row.get("name"))
                .trademark(row.get("trademark"))
                .stock(Integer.valueOf(row.get("stock")))
                .price(new BigDecimal(row.get("price")))
                .description(row.get("description"))
                .tags(row.get("tags"))
                .active(Boolean.valueOf(row.get("active")))
                .build();
        context.setResponse(resources.updateResource(new com.google.gson.Gson().toJson(update), lastResourceId));
    }

    @When("I create a resource with the body:")
    public void iCreateAResourceWithTheBody(String rawJson) {
        context.setResponse(resources.createResourceRaw(rawJson));
    }

    @When("I deactivate every active resource")
    public void iDeactivateEveryActiveResource() {
        context.setResponse(resources.deactivateAllActiveResources());
    }

    // ------------------------------------------------------------------- then

    @Then("the resource in the response has:")
    public void theResourceInTheResponseHas(DataTable table) {
        Resource actual = resources.firstResource(context.getResponse());
        Map<String, String> expected = table.asMaps().get(0);

        assertThat(actual.getName()).isEqualTo(expected.get("name"));
        assertThat(actual.getTrademark()).isEqualTo(expected.get("trademark"));
        assertThat(actual.getStock()).isEqualTo(Integer.valueOf(expected.get("stock")));
        // compareTo, not equals: BigDecimal("99.99") and BigDecimal("99.990") are equal in
        // value and unequal by equals(), and the database decides the scale, not the test.
        assertThat(actual.getPrice()).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal(expected.get("price")));
        assertThat(actual.getDescription()).isEqualTo(expected.get("description"));
        assertThat(actual.getTags()).isEqualTo(expected.get("tags"));
        assertThat(actual.getActive()).isEqualTo(Boolean.valueOf(expected.get("active")));
    }

    @Then("the response matches the resource schema")
    public void theResponseMatchesTheResourceSchema() {
        resources.assertMatchesSchema(context.getResponse(), "schemas/resourceListSchema.json");
    }

    @Then("no resources are active")
    public void noResourcesAreActive() {
        assertThat(resources.resourceList(resources.getActiveResources()))
                .as("every resource should have been deactivated")
                .isEmpty();
    }

    @Then("every resource has a non-negative stock")
    public void everyResourceHasANonNegativeStock() {
        assertThat(resources.resourceList(context.getResponse()))
                .isNotEmpty()
                .allSatisfy(r -> assertThat(r.getStock()).isNotNegative());
    }
}
