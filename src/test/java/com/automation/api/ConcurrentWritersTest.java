package com.automation.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.api.requests.ClientRequest;
import com.automation.api.support.ApiUnderTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

/**
 * What happens when two clients write the same thing at the same time.
 *
 * <p>This is the test the old hosted mock could not have carried in any form. A mock has no
 * unique index, so a race against one is just two requests that both succeed; the interesting
 * behaviour does not exist to be tested. Against a real database it does, and it is worth
 * pinning down, because "the second one gets a 409" is a contract callers build retry logic on.
 *
 * <p>Kept out of Cucumber deliberately. Gherkin describes a sequence of steps, and the whole
 * point here is that two things happen with no order between them — a {@code When} and an
 * {@code And} would misrepresent it as ordered. A plain JUnit test says what is meant.
 */
@Epic("API")
@Feature("Clients")
@Story("Concurrent writes are serialised by the database")
@Severity(SeverityLevel.CRITICAL)
class ConcurrentWritersTest {

    private static final ClientRequest CLIENTS = new ClientRequest();

    @BeforeAll
    static void pointAtTheApi() {
        RestAssured.baseURI = ApiUnderTest.baseUri();
    }

    /**
     * Two inserts of the same unique email, released together.
     *
     * <p>A {@link CyclicBarrier} rather than starting both threads and hoping: without it the
     * first request usually completes before the second is even sent, and the test passes
     * having exercised nothing. The barrier makes both threads wait and then leave at the same
     * instant, which is as close to simultaneous as a JVM can arrange.
     */
    @Test
    @DisplayName("two writers racing to claim one email: exactly one wins, the other gets 409")
    void duplicateInsertRaceHasExactlyOneWinner() throws Exception {
        String contestedEmail = "race.%d@example.com".formatted(System.nanoTime());
        String body =
                """
                {"name": "Racer", "lastName": "Contender", "country": "Colombia", "city": "Cali", "phone": "3000000000", "email": "%s"}"""
                        .formatted(contestedEmail);

        CyclicBarrier startTogether = new CyclicBarrier(2);
        Callable<Response> attempt = () -> {
            startTogether.await(10, TimeUnit.SECONDS);
            return CLIENTS.createClientRaw(body);
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Response>> futures = pool.invokeAll(List.of(attempt, attempt));
            List<Integer> codes =
                    List.of(futures.get(0).get().statusCode(), futures.get(1).get().statusCode());

            assertThat(codes)
                    .as("one insert must win and one must lose; both winning means the unique "
                            + "index is not doing its job, and both losing means neither ran")
                    .containsExactlyInAnyOrder(201, 409);
        } finally {
            pool.shutdownNow();
        }

        // And the database agrees with the HTTP responses: exactly one row exists. A 409 that
        // still wrote a row would be the worst possible outcome and is invisible from status
        // codes alone.
        Response stored = CLIENTS.getClients();
        long matching = CLIENTS.clientList(stored).stream()
                .filter(c -> contestedEmail.equals(c.getEmail()))
                .count();
        assertThat(matching).as("rows actually persisted for the contested email").isEqualTo(1);
    }
}
