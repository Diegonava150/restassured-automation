package com.automation.api.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * The API under test, as two containers this suite starts itself.
 *
 * <p>Postgres holds the data and the constraints; PostgREST turns that schema into a REST
 * API. Together they replace the hosted mock this project used to point at, and the
 * replacement is not a like-for-like swap — it is the point of the exercise:
 *
 * <ul>
 *   <li><b>The suite owns its data.</b> Every run starts from {@code db/init.sql} and nothing
 *       else. The old mock was a single shared instance on the public internet that any run,
 *       from any machine, mutated — and it eventually filled to its 100-row cap and started
 *       rejecting every POST with {@code 400 "Max number of elements reached"}. The suite had
 *       been red for months in a way no code change here could fix.</li>
 *   <li><b>The errors are real.</b> A mock returns the status code it was configured to return.
 *       Postgres returns {@code 409} because a unique index was actually violated, and hands
 *       back the SQLSTATE to prove it. That is the difference between asserting on a number
 *       and asserting on a behaviour.</li>
 *   <li><b>It runs offline.</b> Docker and a JDK, nothing else.</li>
 * </ul>
 *
 * <h2>Why a singleton and not {@code @Testcontainers}</h2>
 *
 * <p>The JUnit 5 {@code @Testcontainers} extension ties container lifecycle to a test class —
 * per-class with a static field, per-method with an instance one. Cucumber has no such class
 * to hang that on: scenarios are generated at runtime by the platform engine, and a step
 * definition is not a test class. Testcontainers documents the singleton pattern for exactly
 * this case, so that is what this is: started once, on first touch, for the life of the JVM.
 *
 * <p>Nothing stops the containers explicitly. Testcontainers' Ryuk sidecar reaps them when the
 * JVM exits, including when it exits badly — which is more reliable than an {@code @AfterAll}
 * that a crashed suite never reaches.
 */
public final class ApiUnderTest {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String POSTGREST_IMAGE = "postgrest/postgrest:v12.2.12";

    private static final String DB_NAME = "apidb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    /** Matches the role created at the bottom of {@code db/init.sql}. */
    private static final String POSTGREST_ROLE = "authenticator";

    private static final String POSTGREST_PASSWORD = "postgrest_pw";
    private static final String ANON_ROLE = "web_anon";
    private static final String SCHEMA = "api";
    private static final int POSTGREST_PORT = 3000;

    private static final Network NETWORK = Network.newNetwork();

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
                    DockerImageName.parse(POSTGRES_IMAGE))
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD)
            .withNetwork(NETWORK)
            .withNetworkAliases("db")
            // Postgres runs everything in /docker-entrypoint-initdb.d once, on first boot.
            // Copied rather than bind-mounted so this works identically on a CI runner,
            // a Windows host, and a remote Docker daemon.
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/init.sql"), "/docker-entrypoint-initdb.d/init.sql");

    private static final GenericContainer<?> POSTGREST = new GenericContainer<>(
                    DockerImageName.parse(POSTGREST_IMAGE))
            .withNetwork(NETWORK)
            .withExposedPorts(POSTGREST_PORT)
            // Reaches Postgres by network alias on the container network, on 5432 — the
            // *internal* port. The mapped host port is irrelevant here and using it is the
            // usual way this wiring is got wrong.
            .withEnv(
                    "PGRST_DB_URI",
                    "postgres://%s:%s@db:5432/%s".formatted(POSTGREST_ROLE, POSTGREST_PASSWORD, DB_NAME))
            .withEnv("PGRST_DB_SCHEMA", SCHEMA)
            .withEnv("PGRST_DB_ANON_ROLE", ANON_ROLE)
            // Readiness is "answers a real request", not "the port is open". PostgREST binds
            // its port before it has loaded the schema cache, and a request in that window
            // fails in a way that looks like a test bug.
            .waitingFor(Wait.forHttp("/clients?limit=1").forPort(POSTGREST_PORT).forStatusCode(200));

    private static boolean started;

    private ApiUnderTest() {}

    /**
     * The base URI to point RestAssured at, starting the stack on first call.
     *
     * <p>Always read at runtime from the mapped port. Docker assigns an ephemeral host port
     * per run, so any hardcoded URL is wrong by construction — and two runs on one machine
     * would collide on a fixed one.
     */
    public static synchronized String baseUri() {
        if (!started) {
            POSTGRES.start();
            POSTGREST.start();
            started = true;
        }
        return "http://%s:%d".formatted(POSTGREST.getHost(), POSTGREST.getMappedPort(POSTGREST_PORT));
    }
}
