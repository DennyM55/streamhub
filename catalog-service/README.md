# Catalog Service

Simple Spring Boot microservice that manages movies for StreamHub.

## Overview
Provides CRUD and search endpoints for movies, backed by PostgreSQL and optionally cached with Redis.

## Requirements
- Java 21
- Maven Wrapper (included)
- PostgreSQL (default: jdbc:postgresql://localhost:5432/streamhub)
- Redis (optional, default host: localhost:6379)

## Configuration
Set values in src/main/resources/application.properties or via environment variables.
Important keys:
- spring.datasource.url
- spring.datasource.username
- spring.datasource.password
- server.port (default 8081)
- spring.data.redis.host
- spring.data.redis.port

## Build
.\mvnw.cmd clean package

## Run
.\mvnw.cmd spring-boot:run

or

java -jar target/catalog-service-0.0.1-SNAPSHOT.jar

## Endpoints
Base path: /movies

- GET /movies?page=0&size=10&search=term&genre=Drama  — paginated list
- GET /movies/{id}                                   — get movie by id
- POST /movies                                      — create movie (201)
- PUT /movies/{id}                                  — update movie
- DELETE /movies/{id}                               — delete movie (204)
- GET /movies/batch?ids=1,2,3                       — get multiple movies by ids

Create request example (JSON):
{
  "title": "Example Movie",
  "description": "Short description",
  "genre": "Drama",
  "releaseYear": 2024,
  "durationMinutes": 120,
  "thumbnailUrl": "https://.../thumb.jpg",
  "mediaUrl": "https://.../media.mp4"
}

## Tests
.\mvnw.cmd test

## References
See HELP.md for quick links to Spring and Maven guides used by this project.
