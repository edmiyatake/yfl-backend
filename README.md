# ŷFL — Financial Forecasting League

ŷFL is a backend platform for running competitive financial-forecasting "seasons" — a game originally built by Professor Savlowitz to test students' market-forecasting skills, and now being rebuilt as a proper multi-tenant web platform for Competitive Analytics and university classrooms alike.

Players form teams, respond to forecast questions ("Will the S&P rise over 2% in the next month?") before a lock time, and get scored automatically when the question resolves. A live leaderboard tracks individual and team standings throughout the season.

This replaces the original implementation, which ran on a single Excel sheet and Alteryx workflow and could only support one group at a time.

## Why this exists

The original system had two structural problems: it couldn't run more than one group without a full rebuild, and all data lived in a single file with no redundancy. This project rebuilds the same game as a proper backend service, designed from the start to:

- Support multiple concurrent seasons without per-group rebuilds
- Scale to ~5,000 registered users
- Enforce role-based access (Student / TA / Professor) at the API level
- Expose a clean versioned API that can serve both a web frontend and a future native mobile client

## Tech stack

- **Backend:** Java 21, Spring Boot 4
- **Database:** PostgreSQL 16, with Flyway for schema migrations
- **Auth:** Spring Security, JWT-based (chosen for portability to a future mobile client)
- **Frontend:** Next.js / TypeScript (separate repo)
- **Local dev:** Docker Compose for Postgres

## Running locally

### Prerequisites

- Java 21
- Maven
- Docker (for local Postgres)

### Steps

1. Clone the repo:
```
git clone <repo-url>
cd yfl-backend
```

2. Start Postgres:
```
docker compose up -d
```

3. Run the app:
```
./mvnw spring-boot:run
mvnw spring-boot:run
```

   Flyway runs migrations automatically on startup.

4. Confirm it's working:
```
curl http://localhost:8080/api/v1/health
```
   Expected response: `DB status: ok`

### Configuration

Database connection settings are read from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`), with local defaults baked in so it runs out of the box against the Docker Compose Postgres instance. No secrets are committed to this repo.

**Port conflict note:** the project expects Postgres on `5432`. If you have a native Postgres install already running locally (common if you've installed it for another class or project), it will silently intercept connections on that port even while the Docker container reports healthy. If you hit unexplained auth failures against a freshly created container, check for a conflicting local service first:
```
netstat -ano | findstr :5432
```

## Project status

Early-stage, actively under development. Phase 0 (scaffolding) is complete: local Postgres via Docker Compose, Flyway migrations, a verified health-check endpoint with test coverage, and a real first commit. Currently moving into Phase 1 (auth, JWT, role-based access).

## Architecture notes

Key decisions made so far, and why:

- **Flyway owns the schema, not Hibernate.** `ddl-auto` is set to `validate` — Hibernate checks the schema matches the entities but never modifies it. All schema changes go through versioned migration files.
- **API versioned from day one** (`/api/v1/...`), so the API can evolve without breaking the future mobile client.
- **Config externalized via environment variables from the start**, to avoid a deployment refactor later.
- **Spring Boot 4 splits autoconfiguration into per-feature artifacts** (e.g. `spring-boot-webmvc-test` for MockMvc support, `spring-boot-flyway` for Flyway autoconfiguration) rather than bundling everything into the older starter dependencies. Worth knowing before assuming a missing class means a missing feature — it usually means a missing module.

A more detailed architecture decision log will be added as the project progresses.