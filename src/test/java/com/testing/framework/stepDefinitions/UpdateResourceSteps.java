package com.testing.framework.stepDefinitions;

import com.api.framework.models.Resource;
import com.api.framework.requests.ResourceRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.util.List;

/**
 * UpdateResourceSteps class contains step definitions for Cucumber scenarios related to updating Resource entities.
 * <p>
 * This class uses {@link ResourceRequest} to perform API requests and validate responses.
 * </p>
 */
public class UpdateResourceSteps {
    private static final Logger logger = LogManager.getLogger(UpdateResourceSteps.class);

    private final ResourceRequest resourceRequest = new ResourceRequest();
    private Response response;
    private Resource lastCreatedResource;

    @Given("there are at least 15 resources in the system")
    public void thereAreAtLeast15ResourcesInTheSystem() {
        response = resourceRequest.getResources();
        logger.info(response.jsonPath().prettify());
        Assert.assertEquals(200, response.statusCode());

        List<Resource> resourceList = resourceRequest.getResourcesEntity(response);
        while (resourceList.size() < 15) {
            response = resourceRequest.createDefaultResource();
            logger.info(response.statusCode());
            Assert.assertEquals(201, response.statusCode());
            resourceList = resourceRequest.getResourcesEntity(resourceRequest.getResources());
        }
    }

    @When("I retrieve the last created resource")
    public void iRetrieveTheLastCreatedResource() {
        response = resourceRequest.getResources();
        logger.info(response.jsonPath().prettify());
        Assert.assertEquals(200, response.statusCode());

        List<Resource> resourceList = resourceRequest.getResourcesEntity(response);
        lastCreatedResource = resourceList.get(resourceList.size() - 1);
        logger.info("Last created resource: " + lastCreatedResource);
    }

    @When("I update all the parameters of the last created resource with the following details")
    public void iUpdateAllTheParametersOfTheLastCreatedResource(String requestBody) {
        Resource updatedResource = resourceRequest.getResourceEntity(requestBody);
        updatedResource.setId(lastCreatedResource.getId());
        response = resourceRequest.updateResource(updatedResource, lastCreatedResource.getId());
        logger.info(response.jsonPath().prettify());
    }

    @Then("the response body should match the resource JSON schema")
    public void theResponseBodyShouldMatchTheResourceJSONSchema() {
        String path = "schemas/resourceSchema.json";
        Assert.assertTrue(resourceRequest.validateSchema(response, path));
        logger.info("Successfully validated schema for Resource object");
    }

    @Then("the response body should contain the updated data")
    public void theResponseBodyShouldContainTheUpdatedData() {
        Resource updatedResource = resourceRequest.getResourceEntity(response);
        Assert.assertEquals(lastCreatedResource.getId(), updatedResource.getId());
        Assert.assertNotEquals(lastCreatedResource, updatedResource);
    }

    @Then("the response should have a status code of {int}")
    public void theResponseShouldHaveAStatusCodeOf(int statusCode) {
        Assert.assertEquals(statusCode, response.statusCode());
    }
}