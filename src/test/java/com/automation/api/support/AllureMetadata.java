package com.automation.api.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Writes the side files that turn a bare Allure report into a useful one.
 *
 * <p>Out of the box the report answers "did it pass". These add the two things a reader actually
 * wants next: <b>what was it run against</b>, and <b>what kind of failure is this</b>. Both are
 * plain files Allure picks up from the results directory — no plugin, no configuration.
 *
 * <p>Written at runtime rather than committed, because the interesting values are only knowable
 * once the containers are up: the resolved base URI and the image tags actually pulled. A
 * committed {@code environment.properties} would be a guess that quietly goes stale.
 */
public final class AllureMetadata {

    private AllureMetadata() {}

    /**
     * Categories map failures onto causes. Allure's defaults only distinguish "failed" from
     * "broken", which tells a reader nothing; these split the failures this suite can actually
     * produce, so a red run is triaged from the report's front page rather than by reading
     * stack traces.
     *
     * <p>The message patterns are matched against the failure text, so they name real strings:
     * SQLSTATE codes from Postgres, the JSON-schema validator's wording, Testcontainers' startup
     * error.
     */
    private static final String CATEGORIES =
            """
            [
              {
                "name": "Constraint violation (the database rejected the data)",
                "matchedStatuses": ["failed"],
                "messageRegex": ".*(23502|23505|23514).*"
              },
              {
                "name": "Schema mismatch (response shape changed)",
                "matchedStatuses": ["failed"],
                "messageRegex": ".*(does not match|object has missing required properties|instance type).*"
              },
              {
                "name": "Wrong status code",
                "matchedStatuses": ["failed"],
                "messageRegex": ".*[Ee]xpected.*[Ss]tatus.*"
              },
              {
                "name": "Container or environment problem (not the API's fault)",
                "matchedStatuses": ["broken"],
                "messageRegex": ".*(Could not find a valid Docker environment|ContainerLaunchException|Connection refused|timed out).*"
              },
              {
                "name": "Product defect",
                "matchedStatuses": ["failed"]
              }
            ]
            """;

    /**
     * Writes {@code environment.properties} and {@code categories.json} into the results
     * directory.
     *
     * <p>Deliberately quiet on failure. Report metadata is a nicety; a suite that cannot write it
     * should still run and still report its actual results.
     */
    public static void write(Path resultsDirectory, String baseUri) {
        try {
            Files.createDirectories(resultsDirectory);

            Properties environment = new Properties();
            environment.setProperty("API.under.test", "PostgREST over PostgreSQL (Testcontainers)");
            environment.setProperty("Base.URI", baseUri);
            environment.setProperty("Postgres.image", ApiUnderTest.postgresImage());
            environment.setProperty("PostgREST.image", ApiUnderTest.postgrestImage());
            environment.setProperty("Java", System.getProperty("java.version"));
            environment.setProperty("OS", "%s %s".formatted(System.getProperty("os.name"), System.getProperty("os.arch")));
            environment.setProperty("Run.location", System.getenv("CI") != null ? "CI" : "local");

            try (var out = Files.newBufferedWriter(
                    resultsDirectory.resolve("environment.properties"), StandardCharsets.UTF_8)) {
                environment.store(out, "Written at runtime by AllureMetadata");
            }

            Files.writeString(resultsDirectory.resolve("categories.json"), CATEGORIES, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write Allure metadata to " + resultsDirectory, e);
        }
    }
}
