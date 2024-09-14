package com.testing.framework.stepDefinitions;

import com.api.framework.utils.Constants;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hooks class contains setup and teardown methods for Cucumber scenarios.
 * <p>
 * This class is used to configure the test environment before and after each scenario.
 * </p>
 */
public class Hooks {
    private static final Logger logger = LogManager.getLogger(Hooks.class);

    /**
     * This method is executed before each scenario.
     * <p>
     * It sets up the base URI for RestAssured and logs the start of the scenario.
     * </p>
     *
     * @param scenario The current Cucumber scenario.
     */
    @Before
    public void testStart(Scenario scenario) {
        logger.info("*****************************************************************************************");
        logger.info("    Scenario: " + scenario.getName());
        logger.info("*****************************************************************************************");
        RestAssured.baseURI = Constants.BASE_URL;
    }

    /**
     * This method is executed after each scenario.
     * <p>
     * It logs the completion of the scenario.
     * </p>
     *
     * @param scenario The current Cucumber scenario.
     */
    @After
    public void cleanUp(Scenario scenario) {
        logger.info("*****************************************************************************************");
        logger.info("    Scenario finished: " + scenario.getName());
        logger.info("*****************************************************************************************");
    }
}