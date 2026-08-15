package com.testing.framework.stepDefinitions;

import com.testing.framework.support.ApiUnderTest;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.ObjectMapperConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Points RestAssured at the container stack before each scenario. */
public class Hooks {

    private static final Logger logger = LogManager.getLogger(Hooks.class);

    /**
     * Resolves the base URI from the running container, starting it on the first scenario.
     *
     * <p>Read every time rather than cached in a field: the value comes from Docker's
     * ephemeral port mapping, so it is only knowable at runtime. This is the single place the
     * suite learns where the API is, and there is no hardcoded URL anywhere for it to fall
     * back to.
     */
    @Before(order = 0)
    public void pointAtTheContainerisedApi(Scenario scenario) {
        RestAssured.baseURI = ApiUnderTest.baseUri();

        // Gson for both directions, matching the request classes. Left to chance, RestAssured
        // would deserialise with Jackson if it happened to be on the classpath, and the two
        // disagree about nulls.
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(io.restassured.mapper.ObjectMapperType.GSON));

        // Only log the request/response when something fails. Logging every call buries the
        // one interaction worth reading in a few hundred that worked.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        logger.info("Scenario '{}' running against {}", scenario.getName(), RestAssured.baseURI);
    }
}
