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
 * ManageResourceSteps class contains step definitions for Cucumber scenarios related to Resource operations.
 * <p>
 * This class uses {@link ResourceRequest} to perform API requests and validate responses.
 * </p>
 */
public class ManageResourceSteps {
    private static final Logger logger = LogManager.getLogger(ManageResourceSteps.class);

    private final ResourceRequest resourceRequest = new ResourceRequest();
    private Response response;
    private List<Resource> resourceList;

    @Given("there are at least 5 active resources in the system")
    public void thereAreAtLeast5ActiveResourcesInTheSystem() {
        response = resourceRequest.getResources();
        logger.info(response.jsonPath().prettify());
        Assert.assertEquals(200, response.statusCode());

        resourceList = resourceRequest.getResourcesEntity(response);
        long activeCount = resourceList.stream().filter(Resource::getActive).count();

        while (activeCount < 5) {
            response = resourceRequest.createDefaultResource();
            logger.info(response.statusCode());
            Assert.assertEquals(201, response.statusCode());
            resourceList = resourceRequest.getResourcesEntity(resourceRequest.getResources());
            activeCount = resourceList.stream().filter(Resource::getActive).count();
        }
    }

    @When("I retrieve the list of all active resources")
    public void iRetrieveTheListOfAllActiveResources() {
        response = resourceRequest.getResources();
        Assert.assertEquals(200, response.statusCode());
        logger.info(response.jsonPath().prettify());

        resourceList = resourceRequest.getResourcesEntity(response);
        resourceList.removeIf(resource -> !resource.getActive());
    }

    @Then("I update all the active resources as inactive")
    public void iUpdateAllTheActiveResourcesAsInactive() {
        for (Resource resource : resourceList) {
            resource.setActive(false);
            response = resourceRequest.updateResource(resource, resource.getId());
            Assert.assertEquals(200, response.statusCode());
            logger.info("Updated resource with ID: " + resource.getId());
        }
    }

    @Then("the response body should match the resources JSON schema")
    public void theResponseBodyShouldMatchTheResourcesJSONSchema() {
        String path = "schemas/resourceListSchema.json";
        Assert.assertTrue(resourceRequest.validateSchema(response, path));
        logger.info("Successfully validated schema for Resource List object");
    }

    @Then("the response should have a status code of {int}")
    public void theResponseShouldHaveAStatusCodeOf(int statusCode) {
        Assert.assertEquals(statusCode, response.statusCode());
    }
}