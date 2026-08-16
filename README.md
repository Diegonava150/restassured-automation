# REST Assured API automation — hermetic, with Testcontainers

[![CI](https://github.com/Diegonava150/restassured-automation/actions/workflows/ci.yml/badge.svg)](https://github.com/Diegonava150/restassured-automation/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](pom.xml)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-Postgres%20%2B%20PostgREST-2496ED?logo=docker&logoColor=white)](src/test/java/com/automation/api/support/ApiUnderTest.java)

API test suite in **Java 21 · REST Assured · Cucumber 7 · JUnit Platform**, running against an
API it starts itself.

```bash
./mvnw test
```

That is the whole setup. No API key, no account, no hosted service, no `.env`, and no Maven
install — the wrapper fetches it. Docker and a JDK.

---

## What is under test, and why that one

**A REST API served by [PostgREST](https://postgrest.org) over a PostgreSQL 16 database, both
started as containers by the suite itself.** The schema is
[`db/init.sql`](src/test/resources/db/init.sql): two tables, `clients` and `resources`, with the
constraints that make the negative tests mean something.

It replaces a hosted mock (`mockapi.io`), and the reason is worth stating plainly, because it is
the whole argument of this repository.

**The mock was a single shared instance on the public internet.** Every run, from every machine,
mutated the same rows. Over time it accumulated records like `AutoClient-1758970437786` and one
resource whose `stock` was the literal string `"Invalid faker method - da..."`. Eventually it hit
the free tier's 100-row ceiling and started answering every create with:

```
HTTP 400  "Max number of elements reached for this resource!"
```

At that point the suite was permanently red, and **no change to this codebase could fix it** —
the failure was in a database belonging to someone else. That is not a flaky test. That is a test
suite with a dependency it does not control and cannot reset.

**A mock also cannot fail honestly.** It returns the status code it was configured to return. Ask
Postgres to insert a duplicate email and it raises a real unique-violation, and PostgREST turns
that into a `409` carrying SQLSTATE `23505`. The negative tests below assert on those codes, so
they are checking that *the database enforced its constraint* — not that a fixture was set up to
return a number.

| Property | Hosted mock | This suite |
| --- | --- | --- |
| Starting state | Whatever the last run left | `init.sql`, every time |
| Runs offline | No | Yes |
| Parallel-safe across machines | No — one shared instance | Yes — a container per run |
| Rejects bad data | No | Yes, with the SQLSTATE |
| Can be permanently broken by someone else | Yes | No |

---

## What the suite covers

**22 Cucumber scenarios plus 3 JUnit tests.** Every happy path has its negative counterpart — that pairing is the point, and
the previous version of this suite had none of the second column.

### `/clients` — [clients.feature](src/test/resources/features/clients.feature)

| Happy path | Paired negative case | Asserts |
| --- | --- | --- |
| List every client | — | `200`, schema, every email non-empty |
| Read one by id | Read an id that does not exist | `200` + empty list (see below) |
| Create a client | Missing required field | `400` · `23502` not-null violation |
| | Duplicate email | `409` · `23505` unique violation |
| | Malformed email | `400` · `23514` check violation |
| | Malformed JSON | `400` · `PGRST102` |
| Update a client | Update a row that does not exist | `200` + empty list |
| | Update into a duplicate email | `409` · `23505` |
| Delete a client | Delete a row that does not exist | `204` — delete is idempotent |

### Two things Gherkin is the wrong shape for

| Test | What it pins down |
| --- | --- |
| [`SchemaValidationHasTeethTest`](src/test/java/com/automation/api/SchemaValidationHasTeethTest.java) | That a violated schema **fails, naming the property**. Every other test asserts schemas *pass*, so none would notice if validation silently broke again — and it had been broken. Points a good response at a deliberately wrong schema and demands an `AssertionError` mentioning the missing field. |
| [`ConcurrentWritersTest`](src/test/java/com/automation/api/ConcurrentWritersTest.java) | Two writers racing for the same unique email: exactly one gets `201`, the other `409`, and **exactly one row persists**. A `CyclicBarrier` releases both at once, because starting two threads and hoping usually means the first finishes before the second is sent. Not expressible in Gherkin, which describes ordered steps — the whole point here is that there is no order. |

The old hosted mock could not have carried either of these: no unique index means no race to lose.

### `/resources` — [resources.feature](src/test/resources/features/resources.feature)

| Happy path | Paired negative case | Asserts |
| --- | --- | --- |
| List every resource | — | `200`, schema, stock never negative |
| Filter to active only | — | server-side `?active=is.true` |
| Update the newest resource | — | `200`, field-by-field, schema |
| Deactivate all active in one request | — | one PATCH, then nothing is active |
| Create a resource | Negative stock | `400` · `23514` |
| | Negative price | `400` · `23514` |
| | Non-numeric stock | `400` — a typed column refuses it |
| | Blank name | `400` · `23514` |

**A note on "not found".** PostgREST answers a filter that matches nothing with `200 []`, not
`404`. A `404` means the *endpoint* does not exist. The suite asserts the real behaviour rather
than the behaviour the old mock happened to have — asserting `404` here would be encoding a
mock's quirk as an API contract.

---

## How to run it

```bash
./mvnw test                                    # everything; starts its own containers
./mvnw test -Dcucumber.filter.tags="@smoke"    # 6-scenario subset, ~10s
./mvnw allure:serve                            # open the Allure report in a browser
```

`@smoke` covers one read, one create and one constraint rejection per endpoint — enough to catch
a broken deployment. It is opt-in: **the default run executes every scenario.** The previous
version of this suite had that backwards, pinning the runner to `@active and @smoke` so anything
untagged never ran and nobody was told.

Reports land in `target/`:

- `target/allure-results/` — raw Allure results, rendered by `mvn allure:serve`
- `target/cucumber/cucumber-report.html` — standalone HTML, opens with no tooling
- `target/cucumber/cucumber.json` — for anything downstream

CI publishes both as artifacts on every run, pass or fail.

### Requirements

- **JDK 21**
- **Docker** — running. First run pulls `postgres:16-alpine` and `postgrest/postgrest:v12.2.12`
  (~440 MB), then they are cached.

The containers start once per JVM and are reaped by Testcontainers' Ryuk sidecar when it exits,
including when it exits badly. Nothing is left running.

---

## How it is wired

```
src/test/java/com/automation/api/
  models/       Client, Resource — types match the database, not a mock's strings
  requests/     BaseRequest + one class per endpoint
  utils/        paths and headers (deliberately no base URL — see below)
  support/      ApiUnderTest (the containers), TestContext (per-scenario state)
  steps/        ClientSteps, ResourceSteps, Hooks
  runners/      RunCucumberTest — the JUnit Platform suite
src/test/resources/
  db/init.sql   the schema, constraints and seed data — i.e. the API itself
  features/     the scenarios
  schemas/      JSON schemas the responses are validated against
  log4j2.properties
```

**Everything is under `src/test`, and that is deliberate.** This repository produces no
production artifact — every class here exists to test something else. The previous layout split
it across `src/main` and `src/test` under two unrelated package roots
(`com.api.framework` and `com.testing.framework`), which forced RestAssured and Gson to compile
scope and made `mvn package` build a jar of test helpers that nothing consumes. One source root,
one package root, and the dependencies scoped honestly.

**There is no base URL anywhere in this repository.** Docker assigns an ephemeral host port on
every run, so the address is only knowable at runtime.
[`ApiUnderTest.baseUri()`](src/test/java/com/automation/api/support/ApiUnderTest.java) reads it
from the mapped port and `Hooks` assigns it to RestAssured before each scenario. A hardcoded URL
would be wrong by construction, and two runs on one machine would collide on a fixed one.

**Singleton containers, not `@Testcontainers`.** The JUnit 5 extension ties container lifecycle to
a test class. Cucumber has no such class — scenarios are generated by the platform engine, and a
step definition is not a test class. Testcontainers documents the singleton pattern for exactly
this case: started once on first touch, reaped at JVM exit.

---

## Deliberately not covered

- **Authentication and authorisation.** PostgREST supports JWT roles; this suite runs everything
  as one anonymous role. Adding auth would mean testing PostgREST's implementation rather than a
  suite's ability to handle it.
- **Performance and load.** Container startup dominates the runtime; any number measured here
  would describe Docker, not the API.
- **PostgREST's own feature surface** — embedded resources, RPC, full-text search, `Range`
  pagination. The suite covers the CRUD contract it defines, not everything the tool can do.
- **Automated bumping of the two container images.** Dependabot covers Maven and GitHub
  Actions here, but the Postgres and PostgREST tags are Java string constants in
  `ApiUnderTest`, which its docker updater cannot read — it parses Dockerfiles and compose
  files, and this repository has neither. Adding a compose file purely to satisfy it would
  commit something nothing runs. Those two pins are bumped by hand.
- **Contract testing against a published spec.** JSON schema validation checks response *shape*;
  it is not a substitute for a consumer-driven contract, and does not claim to be.
- **The `resources` update scenarios do not assert on concurrency.** Nothing here tests what two
  simultaneous writers see. That needs a different harness.

---

## What was changed, and what was verified

This repository was four commits of a template. The rewrite is recorded honestly:

**Verified by running it.** All 25 tests pass locally against Docker in ~11 s, and the same
command runs in CI. Schema validation was verified to *fail* by adding a required property the API
does not return — it produced three failures naming the missing field, which is the check having
teeth rather than being decoration.

**Fixed along the way**, each of which was a real defect:

- Would not compile on JDK 21 at all — Lombok 1.18.28 predates JDK 21 support.
- The pom declared no surefire plugin, and the JUnit 4 `@RunWith(Cucumber.class)` runner while
  also depending on JUnit 5. The suite was one default away from running nothing.
- The runner filtered on `@active and @smoke`, so any scenario missing either tag was silently
  skipped. The tag filter is gone: a scenario in this repository runs.
- `validateSchema` caught the `AssertionError` and returned a boolean, so every schema failure
  read "expected true, was false" and discarded which field was wrong.
- `.idea/` was committed. Now ignored, along with the rest of a real Java/Maven `.gitignore`.
- The README's clone URL was still `https://github.com/yourusername/api-testing-framework.git`.
- A step named *"there are at least 10 registered clients"* **created** clients until there were
  ten. Run against a shared mock with a row cap, that is what eventually broke it. Preconditions
  are now asserted against seeded data, not manufactured.
