# Verification record

Date: 2026-09-25. GitHub Actions passed all four jobs on commit `8323c56ae6ef2d6cb17986c872e59b2bde0bce20`: [verified run](https://github.com/DennyM55/streamhub/actions/runs/36125821385). The final media-fix CI run also passed: [2668c70](https://github.com/DennyM55/streamhub/actions/runs/36134595893). Public hosting was verified on the same date.

| Check | Result | Scope |
| --- | --- | --- |
| API Java tests | 13 passed, 1 skipped | Security, JWT, catalogue retry/circuit breaker, history, event serialization and outbox |
| Catalogue Java tests | 28 passed, 1 skipped | Includes all four native PostgreSQL 16 Testcontainers tests |
| Frontend | 10 tests passed; lint and production build passed | Controlled API fixtures; includes cold-start timeout handling and session-race regression coverage |
| Docker Compose | Passed | Configuration validated locally; all containers built and started in GitHub Actions |
| Render Blueprint syntax | Passed | Validated against the official Render JSON Schema; three staged Blueprints provisioned |
| Both Java packages | Passed | Java 21 executable JARs |
| Integrated HTTP smoke | 10 groups passed | Actual Java applications, Redis 7, Kafka 4.0.2 and a PGlite PostgreSQL-compatible database |
| Redis cache | Confirmed | A real movie lookup created `movies::1` |
| Kafka outbox | Confirmed | Saved progress persisted an outbox row and it was acknowledged/published |
| Kafka consumer | Confirmed | Normal event processing persisted a receipt |
| Duplicate delivery | Confirmed | Delivering the same probe twice produced one durable receipt |
| Dead-letter routing | Confirmed | Invalid event reached `movie-watched.DLT` after retries |
| GitHub Actions / native PostgreSQL 16 | Passed | All 10 HTTP smoke groups passed against the complete Docker stack; durable Kafka consumer receipt confirmed |
| Public hosting | Passed | Render static frontend, two free Docker web services and free cache; Aiven free PostgreSQL 18.6 and Kafka 4.2.1 |
| Hosted HTTP API | All 10 check groups passed | Nine public/user groups, followed by a focused admin CRUD/cache-invalidation check after correcting the admin configuration |
| Hosted Kafka | Confirmed | Browser progress save produced a consumed `MovieWatchedEvent` in Render logs after durable receipt insertion |
| Public frontend browser flows | Passed for tested flows | 12-title catalogue, title search, genre filtering, guest session, favourites, saved history, page reload and video resume; registration/login were verified through the hosted API |

The local integrated run used PGlite (PostgreSQL 18.3 engine through a wire/JDBC adapter), because this execution environment cannot run Docker and native PostgreSQL could not be started. It is not evidence of a successful native PostgreSQL 16 deployment. Four Testcontainers tests and two complete application-context tests were skipped locally. GitHub Actions subsequently executed and passed all four Testcontainers tests on native PostgreSQL 16. Two optional complete application-context tests remain skipped; the Docker smoke run separately starts both full Java applications and verifies real user flows and Kafka delivery.

## Public demo

- Frontend: https://streamhub-dennym55.onrender.com
- API: https://streamhub-api-fvnw.onrender.com
- Catalogue: https://streamhub-catalog.onrender.com

The original Google sample bucket returned HTTP 403. Four media entries were updated through the protected administrator API to working replacement sources, and fresh-install seed URLs were corrected in commits `7664b83` and `2668c70`. Big Buck Bunny resumed at 25 seconds and advanced to 33 seconds in the public browser; Sintel advanced to 10 seconds and Tears of Steel advanced to 8 seconds. All four media entries are explicitly labelled preview clips, while catalogue runtimes describe the full films. Elephants Dream uses a credited 12-second excerpt bundled with the frontend after external mirrors failed in the browser; its playback advanced to 9 seconds in the public browser. The other eight entries demonstrate catalogue and collection operations without a video preview.

Free instances are not always on. Observed Java cold starts took roughly 2–2.5 minutes per service; sequential startup can take longer. The frontend explains startup, allows up to five minutes per request and provides a retry button. Services can suspend at provider quotas; this demo has no production availability SLA. No paid instances were selected.

Frontend registration/login controls and mobile device layouts were not separately exercised in the hosted browser. Authentication APIs, session isolation, admin write protection and all nine claimed capabilities have executable coverage. Two optional full-context Java tests remain skipped as noted above.

Reproduce the public checks using `API_BASE_URL`, `API_TIMEOUT_SECONDS=300` and an administrator key supplied privately to `scripts/smoke.py`. Never commit the key. A build or liveness endpoint alone does not verify user flows.
