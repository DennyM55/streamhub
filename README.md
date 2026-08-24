# StreamHub

Backend API for a streaming-platform style application built with Java 21, Spring Boot, PostgreSQL, Redis, JWT authentication, and a separate catalog microservice.

This project is a learning-focused, production-grade backend build. The goal is to practice real backend engineering patterns step by step: secure authentication, REST API design, persistence, caching, service-to-service communication, resilience, debugging, and repeatable API testing.

## What We Are Building

- A Spring Boot streaming-platform backend with users, movies, favorites, and watch history.
- A separate `catalog-service` running on port `8081`, consumed by the main StreamHub API through Spring `RestClient`.
- A secure API layer using stateless JWT authentication and BCrypt password hashing.
- Redis-backed caching for movie reads, with cache eviction when movie data changes.
- Resilience4j retry and circuit breaker handling around catalog-service calls.
- HTTP request files that act as hands-on API flow checks while building and debugging.

## Current Capabilities

### Authentication and Users

- Register users with validated request payloads.
- Store passwords securely with BCrypt.
- Login returns a signed JWT.
- Protected endpoints require `Authorization: Bearer <token>`.

### Movie Catalog

- Create, read, update, delete movies.
- Search and filter movies with pagination.
- Cache individual movie lookups in Redis.
- Evict cached movie entries on update/delete.
- Retrieve multiple movies by ID through catalog-service batch lookup.

### Favorites

- Add a movie to the authenticated user's favorites.
- Prevent duplicate favorites.
- List favorites with movie titles resolved through catalog-service.
- Remove a favorite by movie ID.

### Watch History

- Save playback progress for the authenticated user.
- Update existing progress for the same user/movie pair.
- Return watch history ordered by latest activity.

### Resilience and Operations

- Main service uses timeouts for catalog-service calls.
- Batch catalog lookups are protected with retry and circuit breaker configuration.
- PostgreSQL and Redis are managed through `docker-compose.yml`.
- Request flow files document and verify expected behavior.

## Architecture

```text
HTTP Client / API Consumer
        |
        v
StreamHub API :8080
  - Users and login
  - JWT authentication
  - Favorites
  - Watch history
  - Local movie APIs
  - Redis cache integration
        |
        | RestClient
        v
Catalog Service :8081
  - Movie catalog CRUD
  - Search/filter/pagination
  - Batch movie lookup
  - Redis-backed movie cache
        |
        v
PostgreSQL + Redis
```

## Tech Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Resilience4j
- JJWT
- Maven Wrapper
- Docker Compose

## Key API Areas

### Users

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/users` | Register a new user |
| `POST` | `/users/login` | Authenticate and return JWT |

### Movies

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/movies` | List/search/filter movies |
| `GET` | `/movies/{id}` | Get one movie |
| `POST` | `/movies` | Create movie |
| `PUT` | `/movies/{id}` | Update movie and evict cache |
| `DELETE` | `/movies/{id}` | Delete movie and evict cache |
| `GET` | `/movies/remote/{id}` | Fetch movie from catalog-service |

### Favorites

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/favorites/{movieId}` | Add movie to authenticated user's favorites |
| `GET` | `/favorites` | List authenticated user's favorites |
| `DELETE` | `/favorites/{movieId}` | Remove movie from authenticated user's favorites |

### Watch History

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `PUT` | `/history/{movieId}` | Save playback progress |
| `GET` | `/history` | Get authenticated user's watch history |

## Local Development

### Prerequisites

- Java 21
- Docker Desktop
- Maven Wrapper from this repository

### Start Infrastructure

```powershell
docker compose up -d
```

This starts:

- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`

### Run StreamHub API

```powershell
.\mvnw.cmd spring-boot:run
```

Main API runs on:

```text
http://localhost:8080
```

### Run Catalog Service

```powershell
cd catalog-service
.\mvnw.cmd spring-boot:run
```

Catalog service runs on:

```text
http://localhost:8081
```

If PostgreSQL rejects the JVM timezone `Asia/Calcutta`, run with a valid PostgreSQL timezone such as:

```powershell
$env:JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"
.\mvnw.cmd spring-boot:run
```

## Request Flow Files

The repository includes HTTP files for repeatable API checks:

- `flow.http` - broader API and JWT workflow checks.
- `favorite-flow-testing.http` - login, favorites, remove favorite, verify removal.
- `redis-flow.http` - cache behavior checks.
- `microservices.http` - service-to-service catalog checks.

The favorite flow validates login, stores the JWT after login, and reuses it in each protected request:

```javascript
client.global.set("auth_token", response.body.token);
```

```http
Authorization: Bearer {{auth_token}}
```

## Progress So Far

- Created the core StreamHub API.
- Added persistent users, movies, favorites, and watch history.
- Added JWT-based security and stateless protected routes.
- Added Redis caching for catalog reads.
- Extracted catalog behavior into a dedicated microservice.
- Added batch movie lookup to reduce repeated catalog calls for favorites.
- Added Resilience4j retry/circuit breaker handling around catalog calls.
- Added API flow files for repeatable manual verification.
- Investigated and fixed favorite-flow testing reliability by separating JWT reuse from downstream catalog availability issues.

## Skills Unlocked

- Spring Boot REST API design.
- Layered backend structure: controller, service, repository, DTO, entity.
- Secure password handling with BCrypt.
- JWT generation, validation, and request authentication filters.
- Stateless Spring Security configuration.
- PostgreSQL persistence with Spring Data JPA.
- Redis caching and cache invalidation.
- Microservice separation with a main API and catalog-service.
- Service-to-service HTTP calls using Spring `RestClient`.
- Timeouts, retry, and circuit breaker patterns with Resilience4j.
- Practical debugging across filters, controllers, services, repositories, and downstream services.
- Manual integration testing with `.http` request flow files.
- Docker Compose infrastructure for PostgreSQL and Redis.
- Git workflow with focused changes and clean project history.

## What We Are Learning

- How to move from a simple CRUD API toward production-style backend architecture.
- How authentication really works inside Spring Security filters.
- Why protected routes should rely on authenticated identity instead of user IDs from URLs.
- How caching improves read paths, and why update/delete operations must evict stale data.
- How microservices fail in real life when downstream services are unavailable.
- How retry and circuit breaker settings affect reliability and error behavior.
- How to debug misleading API failures by tracing request flow end to end.
- How to keep API behavior testable through repeatable request files.

## Next Milestones

- Add automated integration tests for auth, favorites, watch history, and catalog fallback behavior.
- Externalize JWT secret and service URLs into environment-specific configuration.
- Add structured error responses for downstream catalog failures.
- Add OpenAPI documentation.
- Add CI checks for compile and tests.
- Add container images for both services.
