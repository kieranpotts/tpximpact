# Testing strategy

Tests are layered. Fast tests cover logic in isolation, and a small black-box
suite proves the assembled system works.

| Suite                         | Tooling                    | Covers                                                                                           | Needs           |
| ----------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------ | --------------- |
| API service `test`            | JUnit 6                    | Domain, service, and HTTP behavior. Responses validated against the OpenAPI contract. In-memory. | JDK             |
| API service `integrationTest` | JUnit 6, Testcontainers    | The JDBC repository against real PostgreSQL.                                                     | JDK, Docker     |
| Web client                    | Vitest, supertest, cheerio | Routes against a fake API service, and the API client against a local stub server.               | Node.js         |
| Acceptance                    | Playwright                 | The whole system under Docker Compose.                                                           | Docker, Node.js |

Run the first three with `make test`, and acceptance with `make acceptance`.

A few acceptance tests stop and start the system's containers, to show that
data survives restarts and that the API service recovers from a database
outage. They form Playwright's `disruptive` project, which runs one test at a
time, after every other test has finished.

The API service's HTTP tests run through `apiTest`, which starts the service
in memory. Its client validates every response against the contract, and fails
the test on an undocumented path, method, or status code, or on a body of the
wrong media type or shape.

Every requirement in the specification is proved by at least one test.
Acceptance tests are named after requirement IDs, so the test report doubles
as the coverage map.
