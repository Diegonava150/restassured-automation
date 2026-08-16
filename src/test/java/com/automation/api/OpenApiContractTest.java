package com.automation.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.api.support.ApiUnderTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the committed JSON schemas against the description the API generates for itself.
 *
 * <p>PostgREST derives an OpenAPI document from the live database: the tables become paths, the
 * columns become properties, and {@code NOT NULL} becomes {@code required}. So there are two
 * independent statements of the same contract in this repository — the schemas under
 * {@code src/test/resources/schemas}, which are committed and hand-written, and the spec at
 * {@code /}, which is generated from {@code init.sql} at runtime. When they disagree, one of
 * them is lying to whoever reads it, and nothing until now noticed.
 *
 * <p>The gap this closes is nullability. An added column is already caught: the schemas set
 * {@code additionalProperties: false}, so an undeclared field fails validation the moment a
 * response carries it. Dropping a {@code NOT NULL} is invisible by comparison — the spec's
 * {@code required} list shrinks, the committed schema still demands the field, and every
 * scenario keeps passing for as long as the seed data happens to populate it. A consumer who
 * read the schema and concluded "phone is always present" then finds out otherwise in
 * production, which is the wrong place to find out.
 *
 * <p>This is also a test the old hosted mock could not have carried in any form. A mock has no
 * schema to derive a spec from, so there was no second source to disagree with.
 */
@Epic("API")
@Feature("Contract")
@Story("The published spec and the committed schemas describe the same API")
@Severity(SeverityLevel.CRITICAL)
class OpenApiContractTest {

    private static JsonPath spec;

    @BeforeAll
    static void fetchTheSpec() {
        RestAssured.baseURI = ApiUnderTest.baseUri();

        Response response =
                RestAssured.given().accept("application/openapi+json").when().get("/");
        assertThat(response.statusCode())
                .as("PostgREST serves its OpenAPI document at the root")
                .isEqualTo(200);

        spec = response.jsonPath();
    }

    @Test
    @DisplayName("the API publishes a machine-readable description of itself")
    void theApiPublishesASpec() {
        assertThat(spec.getString("swagger")).isEqualTo("2.0");

        Map<String, Object> paths = spec.getMap("paths");
        assertThat(paths.keySet())
                .as("every table PostgREST exposes becomes a path in the spec")
                .contains("/clients", "/resources");
    }

    @Test
    @DisplayName("clientListSchema.json agrees with the spec the database generates")
    void clientsSchemaAgreesWithTheSpec() {
        assertAgrees("clients", "schemas/clientListSchema.json");
    }

    @Test
    @DisplayName("resourceListSchema.json agrees with the spec the database generates")
    void resourcesSchemaAgreesWithTheSpec() {
        assertAgrees("resources", "schemas/resourceListSchema.json");
    }

    /**
     * Property names and required fields, compared as sets so the failure names the exact column
     * rather than reporting that two long strings differ.
     *
     * <p>Two plain tests calling this rather than one {@code @ParameterizedTest}: surefire reports
     * a parameterized case as {@code method(String, String)[1]}, and these test names are read
     * from {@code mutation-expectations.txt}, where an index is both unreadable and silently
     * wrong the moment the rows are reordered.
     */
    private static void assertAgrees(String table, String schemaResource) {
        JsonPath committed = new JsonPath(readClasspath(schemaResource));

        assertThat(namesIn(committed, "items.properties"))
                .as(
                        "columns in %s vs properties in %s - a column added to init.sql without a "
                                + "matching entry here leaves the schema describing an API that no "
                                + "longer exists",
                        table,
                        schemaResource)
                .isEqualTo(namesIn(spec, "definitions.%s.properties".formatted(table)));

        assertThat(requiredIn(committed, "items.required"))
                .as(
                        "NOT NULL columns in %s vs required in %s - this is the one that goes "
                                + "unnoticed, because dropping a NOT NULL keeps every scenario "
                                + "green for as long as the data happens to fill the field in",
                        table,
                        schemaResource)
                .isEqualTo(requiredIn(spec, "definitions.%s.required".formatted(table)));
    }

    private static Set<String> namesIn(JsonPath document, String path) {
        Map<String, Object> properties = document.getMap(path);
        assertThat(properties).as("no properties found at %s", path).isNotNull();
        return new TreeSet<>(properties.keySet());
    }

    private static Set<String> requiredIn(JsonPath document, String path) {
        return new TreeSet<>(document.getList(path, String.class));
    }

    private static String readClasspath(String resource) {
        try (InputStream in =
                OpenApiContractTest.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Not on the test classpath: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
    }
}
