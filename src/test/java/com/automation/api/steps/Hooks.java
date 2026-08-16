package com.automation.api.steps;

import com.automation.api.support.AllureMetadata;
import com.automation.api.support.ApiUnderTest;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.mapper.ObjectMapperType;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Points RestAssured at the container stack before each scenario. */
public class Hooks {

    private static final Logger logger = LogManager.getLogger(Hooks.class);

    /** Allure results land here; kept in step with the surefire system property in the pom. */
    private static final Path ALLURE_RESULTS = Path.of("target", "allure-results");

    private static final AtomicBoolean ONE_TIME_SETUP_DONE = new AtomicBoolean(false);

    /**
     * Resolves the base URI from the running container, starting it on the first scenario.
     *
     * <p>Read every time rather than cached in a field: the value comes from Docker's ephemeral
     * port mapping, so it is only knowable at runtime. This is the single place the suite learns
     * where the API is, and there is no hardcoded URL anywhere for it to fall back to.
     */
    @Before(order = 0)
    public void pointAtTheContainerisedApi(Scenario scenario) {
        String baseUri = ApiUnderTest.baseUri();
        RestAssured.baseURI = baseUri;

        // Gson for both directions, matching the request classes. Left to chance, RestAssured
        // would deserialise with Jackson if it happened to be on the classpath, and the two
        // disagree about nulls.
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(ObjectMapperType.GSON));

        // Attaches the full request and response to the Allure step. Without this the report
        // says which scenarios passed and nothing whatsoever about the HTTP — which is the only
        // reason to open an API report. Registered as a global filter so no call site has to
        // remember it.
        RestAssured.replaceFiltersWith(new AllureRestAssured());

        // Console logging stays failure-only. The Allure attachment covers the successful calls,
        // and dumping every exchange to stdout as well just makes the build log unreadable.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        if (ONE_TIME_SETUP_DONE.compareAndSet(false, true)) {
            // Once the containers are up, so the environment panel reports the real base URI
            // and the images actually pulled rather than a committed guess.
            AllureMetadata.write(ALLURE_RESULTS, baseUri);
        }

        logger.info("Scenario '{}' running against {}", scenario.getName(), baseUri);
    }
}
