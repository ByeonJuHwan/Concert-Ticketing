# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Concert ticketing system built as a microservices architecture (MSA) using Kotlin + Spring Boot. Evolved from a monolith to MSA — the `docs/` directory documents each architectural decision.

## Services & Ports

| Service | HTTP | gRPC | Role |
|---|---|---|---|
| gateway-service | 8000 | — | API Gateway, token validation, routing |
| concert-service | 8080 | 9093 | Concerts, seats, reservations, search |
| user-service | 8081 | 9091 | Users, point balance |
| token-service | 8082 | — | Queue tokens, waiting list |
| payment-service | 8083 | 9094 | Payments, SAGA orchestration |
| eureka-service | 8761 | — | Service discovery |
| config-service | 8888 | — | Centralized config via Spring Cloud Config |

## Build & Run Commands

Each service is an independent Gradle project. Run commands from within the service directory:

```bash
# Build a service
cd concert-service && ./gradlew build

# Run tests for a specific service
cd concert-service && ./gradlew test

# Run a single test class
cd concert-service && ./gradlew test --tests "org.ktor_lecture.concertservice.SomeTest"

# Start infrastructure (DB, Redis, Kafka, Elasticsearch, Kibana, Kafka UI)
docker-compose up -d

# Run a specific service
cd concert-service && ./gradlew bootRun
```

## Architecture Patterns

All microservices use **Hexagonal (Ports & Adapters)** architecture:
```
adapter/in/       ← Controllers, Kafka consumers, batch jobs
adapter/out/      ← JPA repositories, Kafka producers, Redis, gRPC clients, Elasticsearch
application/      ← Service interfaces, DTOs, port definitions
domain/           ← Entities, value objects, domain events
config/           ← Spring configuration
```

**Inter-service communication**:
- gRPC for synchronous calls (Concert ↔ User ↔ Payment via `.proto` stubs)
- Kafka for async events (outbox pattern for reliability)
- Proto stubs come from a separate GitHub Packages artifact: `com.concert:concert-proto:1.0.3`

**SAGA pattern** in `payment-service/application/service/saga/` handles distributed transactions across Payment, Concert, and User services with compensation logic.

**Caching** is two-tier: Caffeine (local, L1) + Redis (distributed, L2). Concert service has both; other services use Redis only.

**Config broadcast**: Config service uses Spring Cloud Bus (Kafka) for `/actuator/busrefresh` to propagate config changes to all services without restart.

## Testing

Tests use **TestContainers** — they spin up real MariaDB, Kafka, Redis, and Elasticsearch containers. No mocking of infrastructure.

Each service with integration tests has:
- `IntegrationTestBase.kt` — TestContainers setup with `withReuse(true)` for speed
- `application-test.yml` — DDL `create-drop`, discovery disabled
- Tests activate with the `test` Spring profile

```bash
# Requires Docker running; containers are reused between runs
cd concert-service && ./gradlew test
```

## Infrastructure (docker-compose.yml)

- **MariaDB** (3306): Three databases — `concert`, `user`, `queue_token`. Credentials: `root/1234`
- **Redis** (6379): Custom image from `redis/Dockerfile`
- **Kafka** (9092): Confluent 7.5.0, single-node KRaft mode (no Zookeeper)
- **Kafka UI** (8989): Web UI for topic inspection
- **Elasticsearch** (9200): v8.17.4 with Nori plugin (Korean NLP), security disabled
- **Kibana** (5601): Connects to Elasticsearch

## Key Design Decisions (see docs/ for full context)

- **`docs/synchronicity.md`** — Distributed locking strategies for seat reservation (Redis + DB-level)
- **`docs/msa/msa-saga.md`** — Why SAGA was chosen over 2PC for payment consistency
- **`docs/grpc/grpc.md`** — REST→gRPC migration rationale and performance results
- **`docs/caching.md`** — Caffeine + Redis layered cache strategy
- **`docs/outbox.md`** — Outbox pattern for Kafka event reliability
- **`docs/msa/circuit-breaker.md`** — Resilience4j integration per service

## Performance Testing

K6 scripts are in `performance/`. Run against local or deployed services:
```bash
k6 run performance/concert_reservation_test.js
```
Benchmarks are tracked in `docs/performance.md` and summarized in `README.md`.

## GitHub Packages

The proto library requires GitHub token authentication. Add to `~/.gradle/gradle.properties`:
```
gpr.user=<github-username>
gpr.token=<github-token>
```
Each service's `build.gradle.kts` references this via `providers.gradleProperty("gpr.token")`.
