package com.automation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.automation.api.requests.ClientRequest;
import com.automation.api.support.ApiUnderTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Proves the JSON schema check can fail, and says something useful when it does.
 *
 * <p>Every other scenario in this repository asserts a schema <em>passes</em>. Not one of them
 * would notice if schema validation quietly stopped working — and it had. The original
 * implementation caught the {@link AssertionError}, returned {@code false}, and let an
 * {@code assertTrue} report "expected true, was false", discarding which field was wrong.
 * Validation that cannot fail is decoration, and validation that fails without saying why is
 * barely better.
 *
 * <p>So this test does the opposite of the rest of the suite: it points a known-good response at
 * a schema that is deliberately wrong ({@code deliberatelyWrongClientSchema.json}) and insists on
 * a failure that names the missing property. If someone reintroduces the swallow, this goes red
 * rather than the whole suite going quietly green.
 */
class SchemaValidationHasTeethTest {

    private static final ClientRequest CLIENTS = new ClientRequest();

    /**
     * This is not a Cucumber scenario, so {@code Hooks} never runs — the base URI has to be set
     * here. {@link ApiUnderTest} is a singleton, so this reuses whatever the Cucumber suite
     * already started rather than paying for a second stack.
     */
    @BeforeAll
    static void pointAtTheApi() {
        RestAssured.baseURI = ApiUnderTest.baseUri();
    }

    @Test
    @DisplayName("a response that matches its schema passes")
    void matchingSchemaPasses() {
        Response response = CLIENTS.getClients();
        assertThat(response.statusCode()).isEqualTo(200);

        assertThatCode(() -> CLIENTS.assertMatchesSchema(response, "schemas/clientListSchema.json"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a response that violates its schema fails, naming the offending property")
    void violatedSchemaFailsAndSaysWhy() {
        Response response = CLIENTS.getClients();
        assertThat(response.statusCode()).isEqualTo(200);

        assertThatThrownBy(
                        () -> CLIENTS.assertMatchesSchema(response, "schemas/deliberatelyWrongClientSchema.json"))
                .isInstanceOf(AssertionError.class)
                // The message is the point. "expected true, was false" would satisfy the
                // isInstanceOf check above and tell a reader nothing at all.
                .hasMessageContaining("nationality");
    }
}
