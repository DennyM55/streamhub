# Verification record

Date: 2026-09-25. This record separates executable evidence from deployment work still pending.

| Check | Result | Scope |
| --- | --- | --- |
| API Java tests | 13 passed, 1 skipped | Security, JWT, catalogue retry/circuit breaker, history, event serialization and outbox |
| Catalogue Java tests | 24 passed, 5 skipped | Paging/filtering, mutation authorization, seed data and service behaviour |
| Frontend | 6 tests passed; lint and production build passed | Controlled API fixtures; includes cold-start timeout handling |
| Docker Compose configuration | Passed | Validated by Docker Compose v2.40.3; daemon execution pending |
| Both Java packages | Passed | Java 21 executable JARs |
| Integrated HTTP smoke | 10 groups passed | Actual Java applications, Redis 7, Kafka 4.0.2 and a PGlite PostgreSQL-compatible database |
| Redis cache | Confirmed | A real movie lookup created `movies::1` |
| Kafka outbox | Confirmed | Saved progress persisted an outbox row and it was acknowledged/published |
| Kafka consumer | Confirmed | Normal event processing persisted a receipt |
| Duplicate delivery | Confirmed | Delivering the same probe twice produced one durable receipt |
| Dead-letter routing | Confirmed | Invalid event reached `movie-watched.DLT` after retries |
| GitHub Actions / native PostgreSQL 16 | Pending | Workflow added; remote execution must confirm Docker and native database behaviour |
| Public hosting | Pending | Render, Vercel and Aiven provisioning and credentials |
| Public frontend browser flows | Pending | Catalogue, guest/login, favourites, playback, progress and reload |

The local integrated run used PGlite (PostgreSQL 18.3 engine through a wire/JDBC adapter), because this execution environment cannot run Docker and native PostgreSQL could not be started. It is not evidence of a successful native PostgreSQL 16 deployment. Four Testcontainers tests and two complete application-context tests were skipped locally. The complete API smoke run does start both Java applications against running dependencies.

The test definitions are in the repository. Run the public smoke script after deployment and update the pending rows only after obtaining actual results. A build or liveness endpoint alone does not verify user flows.
