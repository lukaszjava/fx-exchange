# FX Exchange – PLN ⇄ USD

> A Spring Boot 3 application exposing a REST API to **open currency accounts** and **exchange PLN ⇄ USD** using the **current mid rate fetched from the public NBP API**. Designed with clean, testable boundaries (ports & adapters), resilient outbound calls, optimistic locking, rate‑limiting, and production‑grade operational concerns (health, metrics, Prometheus, Swagger / OpenAPI).

---

## Table of Contents

* [Architecture](#architecture)
* [Tech Stack](#tech-stack)
* [Requirements Coverage](#requirements-coverage)

    * [Functional](#functional)
    * [Non-Functional](#non-functional)
* [API & Swagger](#api--swagger)

    * [Endpoints](#endpoints)
    * [Problem Details (RFC 7807)](#problem-details-rfc-7807)
    * [Examples](#examples)
* [Getting Started](#getting-started)

    * [Prerequisites](#prerequisites)
    * [Run with Docker Compose (recommended)](#run-with-docker-compose-recommended)
    * [Run locally (no Docker)](#run-locally-no-docker)
    * [Configuration](#configuration)
    * [Database Migrations](#database-migrations)
* [Testing](#testing)

    * [Test Pyramid](#test-pyramid)
    * [How to run](#how-to-run)
    * [Coverage](#coverage)
* [Operational Concerns](#operational-concerns)

    * [Security](#security)
    * [Rate Limiting](#rate-limiting)
    * [Resilience (Retry, Circuit Breaker)](#resilience-retry-circuit-breaker)
    * [Persistence & Concurrency](#persistence--concurrency)
    * [Observability](#observability)
* [CI/CD & Quality Gates](#cicd--quality-gates)
* [Known Limitations & Future Work](#known-limitations--future-work)
* [Contributing](#contributing)
* [License](#license)

---

## Architecture

**Pattern**: Ports & Adapters (a.k.a. Hexagonal).
**Layers**:

* **Domain** (`com.example.fx.domain`): pure business logic – `Account`, `Direction`, `ExchangeService`, domain exceptions.
* **Application** (`com.example.fx.application`): use cases & orchestrators – `CreateAccountUseCase`, `ExchangeFundsUseCase`, `AccountService`, `ExchangeFundsHandler`.
* **Infrastructure** (`com.example.fx.infrastructure`): adapters

    * **Web** (REST controllers, DTOs, exception → ProblemDetails mapper)
    * **Persistence** (JPA repository, MapStruct mappers, Flyway migrations)
    * **NBP client** (OpenFeign + Resilience4j)
    * **Security**, **CORS**, **rate-limiting** (Bucket4j)
* **Config** (`com.example.fx.config`): Spring wiring for domain/application beans.

**Key design decisions**

* **Domain-first**: domain model & services are framework-agnostic.
* **Optimistic locking** via JPA `@Version` to protect concurrent updates.
* **Resilience4j** around the NBP API (retry + circuit breaker).
* **Problem Details (RFC 7807)** for consistent error responses.
* **Rate limiting** to protect the app under load/misuse.
* **Actuator + Prometheus** for metrics & health.
* **Swagger / OpenAPI** auto-generated & exposed.

---

## Tech Stack

* **Language / Runtime**: Java **21**
* **Framework**: Spring Boot **3.3.2**, Spring Cloud **2023.0.3**
* **HTTP Client**: OpenFeign
* **Resilience**: Resilience4j
* **Persistence**: Spring Data JPA, **PostgreSQL**, **Flyway**
* **Object Mapping**: MapStruct
* **Validation**: Jakarta Validation
* **Security**: Spring Security (headers hardening, CSP, HSTS), CORS
* **Rate limiting**: Bucket4j
* **Build**: **Maven**, Docker (multi-stage)
* **Testing**: JUnit 5, AssertJ, Spring MockMvc, **Testcontainers** (PostgreSQL), WireMock, Jacoco, Spotless

---

## Requirements Coverage

### Functional

| #  | Requirement                                                       | How it’s satisfied                                                                          | Endpoint / Component                                      | Tests                                                                                      |
| -- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| F1 | REST API to **open a currency account**                           | `POST /accounts` accepts first/last name & initial PLN; returns account with generated UUID | `AccountController#create`, `AccountService#create`       | `AccountApiIT#create_account_success`, `AccountServiceTest#create_persists_and_returns_id` |
| F2 | **Initial PLN balance** must be provided on account creation      | `CreateAccountRequest.initialPlnBalance` with `@DecimalMin("0.00")` validation              | Validation layer + `AccountService#create`                | `AccountApiIT#invalidCreatePayloads`                                                       |
| F3 | **First & last name required**                                    | `@NotBlank` on DTO + validation advice → 400 with details                                   | Validation + `ProblemHandler#handleValidation`            | `AccountApiIT#create_account_validation_error`                                             |
| F4 | **Account identifier is generated** and used for subsequent calls | `UUID.randomUUID()` in `AccountService#create`, returned in response, used in URLs          | `AccountService#create`                                   | Covered by creation / retrieval ITs                                                        |
| F5 | REST API to **exchange PLN ⇄ USD** using **current NBP rate**     | `POST /accounts/{id}/exchange`; `NbpExchangeRateProvider` fetches rate via OpenFeign        | `ExchangeController`, `ExchangeFundsHandler`, `NbpClient` | `ExchangeApiIT#exchange_pln_to_usd_success`, `exchange_usd_to_pln_success`                 |
| F6 | REST API to **fetch account details** in PLN & USD                | `GET /accounts/{id}`                                                                        | `AccountController#get`                                   | `AccountApiIT#create_account_success` (GET after POST), `get_notFound`                     |

### Non-Functional

| #   | Requirement / Concern              | How it’s satisfied                                                                                                                                                          |
| --- | ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| N1  | **Java**                           | Implemented in Java **21**                                                                                                                                                  |
| N2  | **Any framework**                  | Spring Boot 3                                                                                                                                                               |
| N3  | **Data persisted across restarts** | PostgreSQL + JPA; Flyway migration `V1__init.sql`; Docker Compose with a named volume                                                                                       |
| N4  | **Code hosted in a VCS**           | (Assumed GitHub/GitLab/Bitbucket – add link in your repo)                                                                                                                   |
| N5  | **Build with Maven/Gradle**        | **Maven** – `mvn clean verify`; plugins: Surefire/Failsafe, Jacoco, Spotless, Spring Boot repackage                                                                         |
| N6  | **README with instructions**       | This document                                                                                                                                                               |
| N7  | **Style / code quality**           | Spotless (Google Java Format), layered architecture, MapStruct for mapping, ProblemDetails for errors, resilience, bucket4j, optimistic locking                             |
| N8  | **Security / Hardening**           | Spring Security headers (CSP, HSTS, XSS protection, Referrer-Policy), CORS allow-all for demo, rate-limiter, validation, actuator exposure restricted to selected endpoints |
| N9  | **Resilience**                     | Resilience4j retry + circuit breaker around NBP API                                                                                                                         |
| N10 | **Observability**                  | Actuator (health, info, prometheus), Micrometer Prometheus registry                                                                                                         |
| N11 | **Concurrency control**            | Optimistic locking through JPA `@Version` + explicit expectedVersion check                                                                                                  |

---

## API & Swagger

* **Swagger UI (local)**: `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Endpoints

| Method | Path                             | Description                        | Auth |
| ------ | -------------------------------- | ---------------------------------- | ---- |
| POST   | `/accounts`                      | Create an account                  | none |
| GET    | `/accounts/{id}`                 | Get account details                | none |
| POST   | `/accounts/{id}/exchange`        | Exchange PLN ⇄ USD                 | none |
| GET    | `/actuator/health`               | Liveness/Readiness                 | none |
| GET    | `/actuator/prometheus`           | Prometheus metrics scrape endpoint | none |
| GET    | `/v3/api-docs`, `/swagger-ui/**` | OpenAPI / Swagger UI               | none |

### Problem Details (RFC 7807)

Errors are returned as **`application/problem+json`** with consistent shape:

* `type` – problem URI (`https://example.com/problems/...`)
* `title` – short error label
* `status` – HTTP status code
* `detail` – human readable explanation
* `instance` – request path
* custom properties (e.g. `errors` for validation)

Handled cases:

* `AccountNotFoundException` → 404 (`/problems/account-not-found`)
* `InsufficientFundsException` → 409 (`/problems/insufficient-funds`)
* Validation errors → 400 (`/problems/validation-error`)
* Any other → 500 (`/problems/internal-error`)

### Examples

#### 1) Create account

**Request**

```http
POST /accounts
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "initialPlnBalance": 123.45
}
```

**Response** `201 Created`

Headers:

```
Location: /accounts/{uuid}
```

Body:

```json
{
  "accountId": "f1f7a7d8-...-...",
  "firstName": "John",
  "lastName": "Doe",
  "balances": {
    "PLN": 123.45,
    "USD": 0.0000
  }
}
```

#### 2) Get account

```http
GET /accounts/{id}
```

**200 OK**

```json
{
  "accountId": "f1f7a7d8-...-...",
  "firstName": "John",
  "lastName": "Doe",
  "balances": {
    "PLN": 123.45,
    "USD": 0.0000
  }
}
```

#### 3) Exchange PLN → USD

```http
POST /accounts/{id}/exchange
Content-Type: application/json

{
  "direction": "PLN_TO_USD",
  "amount": 333.33
}
```

**200 OK**

```json
{
  "balances": {
    "PLN": 666.67,
    "USD": 80.8403
  },
  "rate": 4.1234,
  "rateTimestamp": "2024-01-01T12:34:56.789Z"
}
```

#### 4) Validation error (400)

```json
{
  "type": "https://example.com/problems/validation-error",
  "title": "Bad Request",
  "status": 400,
  "instance": "/accounts",
  "errors": [
    { "field": "firstName", "message": "must not be blank" }
  ]
}
```

#### 5) Insufficient funds (409)

```json
{
  "type": "https://example.com/problems/insufficient-funds",
  "title": "Insufficient funds",
  "status": 409,
  "detail": "Not enough PLN to exchange 100.00 (available: 10.00)",
  "instance": "/accounts/{id}/exchange"
}
```

---

## Getting Started

### Prerequisites

* **JDK 21**
* **Maven 3.9+**
* **Docker & Docker Compose** (if you want to run infra locally quickly)

The app will be available at `http://localhost:8080`.

### Run locally

1. Export env vars (or use `application.yml` defaults):

   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fx
   export SPRING_DATASOURCE_USERNAME=fx
   export SPRING_DATASOURCE_PASSWORD=fx
   ```

2. Run:

   ```bash
    mvn clean install
    docker-compose up --build
   ```

### Configuration

Key properties (see `application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fx
    username: fx
    password: fx
  flyway:
    enabled: true

nbp:
  api-url: https://api.nbp.pl

resilience4j:
  retry:
    instances:
      nbpRate:
        max-attempts: 3
        wait-duration: 500ms
  circuitbreaker:
    instances:
      nbpRate:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50

management:
  endpoints:
    web.exposure.include: health,info,prometheus

rate-limit:
  capacity: 100
  refill: 100
  window: PT1M
```

You can override any of them with standard Spring mechanisms (env vars, system props, profiles).

### Database Migrations

* **Flyway** runs automatically at application startup.
* Base migration: `src/main/resources/db/migration/V1__init.sql`.

---

## Testing

### Test Pyramid

* **Unit tests** (pure Java, no Spring context)

    * `AccountServiceTest`
    * `ExchangeServiceTest`
    * `AccountEntityMapperTest`
* **Integration tests** (Spring context + external deps mocked or real)

    * **REST API** with **MockMvc**, **WireMock** (NBP), **Testcontainers** (PostgreSQL)

        * `AccountApiIT`
        * `ExchangeApiIT`
    * **Repository / JPA** with Testcontainers

        * `JpaAccountRepositoryIT`

### How to run

```bash
# Run unit tests only
mvn -Dtest=*Test test

# Run integration tests only
mvn -Dit.test=*IT verify

# Run everything + coverage report
mvn clean verify
```

### Coverage

* **JaCoCo** is configured. After `mvn verify`, open:

  ```
  target/site/jacoco/index.html
  ```
* (Optional) Gate coverage % in CI (e.g., via `jacoco:check`) – currently not enforced.

---

## Operational Concerns

### Security

* **Spring Security** with:

    * CSP (`default-src 'self'`)
    * HSTS (1 year, include subdomains)
    * XSS protection, Referrer-Policy: no-referrer, FrameOptions: sameOrigin
* **CORS**: allow-all for demo purposes (`CorsConfig`). Tighten in production.
* **Actuator exposure** limited to `health`, `info`, `prometheus`.
* **CSRF disabled** (stateless API). Consider enabling when needed.

### Rate Limiting

* Implemented via **Bucket4j** (`RateLimitFilter`).
* Configurable via:

    * `rate-limit.capacity`
    * `rate-limit.refill`
    * `rate-limit.window`
* On exceeding the limit:

    * Returns **429** with `Retry-After` header.
    * Returns `X-Rate-Limit-Remaining` when successful.

### Resilience (Retry, Circuit Breaker)

* **Resilience4j** around NBP API:

    * **Retry**: `max-attempts=3`, `wait-duration=500ms`
    * **CircuitBreaker**: sliding window 10, failure threshold 50%, minimum 5 calls
* Tested in `ExchangeApiIT` with `CircuitBreakerRegistry`.

### Persistence & Concurrency

* **PostgreSQL** (Testcontainers for tests, docker-compose for local, configurable for prod).
* **JPA** with **optimistic locking** (`@Version`).
* `JpaAccountRepository#update` checks `expectedVersion` and throws `OptimisticLockingFailureException` on mismatch (covered in `JpaAccountRepositoryIT#optimisticLocking_shouldThrow`).

### Observability

* **Spring Boot Actuator**:

    * `/actuator/health`, `/actuator/info`, `/actuator/prometheus`
* **Micrometer + Prometheus** registry.
* **Structured logging** (Logstash encoder dependency is present; wire up JSON appender in `logback-spring.xml` if desired).

---

## CI/CD & Quality Gates

**Suggested pipeline stages:**

1. **Verify formatting** (Spotless) – runs automatically during `validate`.
2. **Static analysis** (SpotBugs, PMD, SonarQube – can be added).
3. **Unit tests** (Surefire) + **coverage** (JaCoCo).
4. **Integration tests** (Failsafe) using **Testcontainers**.
5. **Build Docker image** & push.
6. **Deploy** (Kubernetes / VM / ECS / etc).

**Conventions**

* Consider **Conventional Commits** + **SemVer** for releases.
* Generate **CHANGELOG.md** automatically (e.g., `semantic-release`).

---

## Known Limitations & Future Work

* **No authentication / authorization** – intentionally open for the exercise; add OAuth2/JWT in production.
* **NBP API single rate only (USD)** – extend to multiple currencies / pairs.
* **No pagination / listing endpoints** (e.g., list accounts).
* **No idempotency keys** for POSTs – add if required (especially important for money ops).
* **No audit logging** of exchanges – consider for traceability & compliance.
* **No K6/JMeter performance tests** – can be added.
* **No explicit SLOs/SLAs or alerting rules** – define with Prometheus/Alertmanager or your APM.
* **Simple rate limiting (per-IP)** – improve to user-scoped / token-scoped if auth is introduced.

---

## Contributing

1. Fork & clone the repo.
2. Create a feature branch.
3. Run `mvn clean verify` before pushing.
4. Open a PR; ensure checks pass & provide a clear description.

---

## License

Add your preferred license (e.g., MIT, Apache-2.0).
