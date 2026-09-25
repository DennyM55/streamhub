# Deployment

The chosen demo route is Vercel Hobby for the frontend, Render Free for the two Java services and cache, and Aiven's free PostgreSQL and Apache Kafka tiers. The Dockerfiles and Compose setup remain portable. Account access and actual service provisioning are required before there is a live URL.

## Free-tier constraints

- Render's free web services sleep after 15 minutes without inbound traffic, wake on a request, and share 750 running hours per workspace/month (including other projects). The UI supports retries while services start.
- Free Render services cannot receive private-network requests. Set `CATALOG_URL` to the catalogue service's public HTTPS origin. Its write endpoints still require the private catalogue key.
- Aiven free Kafka supports up to five topics, with two partitions each. Create `movie-watched` and `movie-watched.DLT` in the Aiven Console; set `KAFKA_AUTO_CREATE_TOPICS=false` and `KAFKA_TOPIC_PARTITIONS=2`.
- Aiven can power off inactive free services. Its free tiers do not require a card and have no fixed expiry; choose **Free**, not a paid plan funded by trial credits.
- The free Render database expires after 30 days, so use external Aiven PostgreSQL for a longer-lived demo. Create separate logical databases for the two apps when available.
- These limits mean the demo is free within quotas, with cold starts and possible suspension. It is not an always-on production service.

References: [Render Free](https://render.com/docs/free), [Aiven Kafka Free](https://aiven.io/docs/products/kafka/free-tier/kafka-free-tier), [Aiven free services](https://aiven.io/docs/platform/concepts/service-pricing).

## Render setup

Deploy in two stages so the API receives the actual catalogue URL. First import the root `render.yaml`: it creates the free catalogue web service, free cache and shared generated service key. Once the catalogue is live, import `deploy/render-api.yaml` as a second Blueprint in the same workspace; it references the existing cache and environment group and creates the free API with generated JWT/admin secrets. Supply the real catalogue HTTPS URL, database/Kafka credentials and the frontend origin. For initial API verification before frontend hosting, the existing local development origin `http://localhost:5173` can be used; replace it with the final Vercel origin before testing the public frontend. Existing Render resource names must be checked before importing either blueprint to avoid modifying an unrelated service.

For Aiven Kafka, use the exact SASL mechanism and JAAS connection settings shown by its service. The blueprint uses `SPRING_KAFKA_SSL_TRUSTSTORETYPE=PEM` and prompts for `SPRING_KAFKA_SSL_TRUSTSTORECERTIFICATES`: paste the service CA certificate as a multiline PEM value, including its BEGIN/END lines. If the selected endpoint instead uses a publicly trusted CA, remove both PEM variables to use the JVM trust store. Keep TLS hostname/certificate validation enabled. Never put these values in Git or frontend build variables.

## Vercel frontend

- Import `DennyM55/streamhub`, root directory `frontend`, framework Vite.
- Set `VITE_API_BASE_URL` to the public HTTPS URL of the deployed API, without a trailing slash.
- Build: `npm run build`. Output directory: `dist`.
- Set the backend `CORS_ORIGIN` to the exact public frontend origin. Redeploy the API after changing this.

## Java services

| Setting | API | Catalogue |
| --- | --- | --- |
| Repository root directory | `/` | `/catalog-service` |
| Dockerfile | `Dockerfile` | `Dockerfile` |
| Port | `8080` | `8081` |
| Public domain | HTTPS domain required | HTTPS on free Render; mutations protected by service key |
| Healthcheck | `/health` | `/health` |
| Database | Separate users database | Separate catalogue database |

Both `/health` endpoints indicate application liveness. The HTTP smoke test verifies dependency availability and real flows.

### Environment variables

Use separate database credentials in the two services. `DATABASE_URL` must be a JDBC URL, for example `jdbc:postgresql://<private-host>:5432/<database>`; a provider's plain `postgresql://` URL is not interchangeable.

| Variable | Where | Value |
| --- | --- | --- |
| `DATABASE_URL` | Both | JDBC URL for that service's database |
| `DATABASE_USER`, `DATABASE_PASSWORD` | Both | Database credentials |
| `REDIS_HOST`, `REDIS_PORT` | Both | Private Redis host and port |
| `REDIS_PASSWORD` | Both | Redis password when enabled |
| `CATALOG_API_KEY` | Both | Same random secret of at least 32 characters |
| `CATALOG_URL` | API | Catalogue HTTPS origin on Render |
| `KAFKA_BOOTSTRAP_SERVERS` | API | Broker host and port supplied by Aiven |
| `JWT_SECRET` | API | Random secret of at least 32 characters; required |
| `ADMIN_API_KEY` | API | Random secret used only by trusted administrative tooling |
| `CORS_ORIGIN` | API | Exact frontend HTTPS origin |
| `STREAMHUB_SEED_DEMO` | Catalogue | `true` for the first demonstration deployment |
| `PORT` | Both | `8080` for API; `8081` for catalogue |

Keep the administrative and catalogue keys out of frontend build variables. Browser visitors can browse and create isolated guest sessions but cannot mutate the shared catalogue. All catalogue reads and mutations from the edge API use the catalogue service; user/history data remain in the users database. The blueprint uses `SPRING_DATA_REDIS_URL` from Render's private cache connection instead of individual Redis host/port values.

## Kafka

The local Compose configuration uses Apache Kafka `4.0.2`, a single broker in KRaft mode, three partitions and replication factor one. The hosted free Aiven configuration uses its managed broker version, two partitions and broker-managed replication. The API must reach the advertised broker hostname, not just the bootstrap address.

For hosted PostgreSQL, set `POSTGRES_CA_CERT` to its public CA PEM and use `?sslmode=verify-full&sslrootcert=/tmp/streamhub-postgres-ca.pem` in each JDBC URL. The container entrypoint writes the certificate with owner-only permissions before starting Java. This verifies both the CA and the database hostname.

Keep Redis private. Externally hosted PostgreSQL and Kafka require authentication and TLS. The local single-broker demo has no broker redundancy.

## Verification after deployment

Run against the public API:

```bash
API_BASE_URL=https://your-api-host python3 scripts/smoke.py
```

The optional `ADMIN_API_KEY` environment variable enables additional catalogue create/update/delete checks. Tests create disposable user sessions and one disposable catalogue record. Secrets and bearer tokens are never printed by the script.

Finally open the frontend in a fresh browser, create a guest session, save a favourite, play an available open film, save progress, reload, and confirm persistence. A successful build or `/health` response alone is not full verification.

## Local integrated demo

```bash
bash scripts/setup-local.sh
docker compose up --build -d
```

Open `http://localhost:8088`. Only the frontend is exposed beyond localhost. This Compose project uses a new `streamhub-demo` project name and new volumes; it does not migrate or replace the earlier local development database. `docker compose down` keeps data; `docker compose down -v` deletes this demo's data.
