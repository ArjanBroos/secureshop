# Architecture

_Last updated: 2026-05-21 (Slice 0)_

## Overview

A webshop consisting of a Java Spring Boot backend API and a React (TypeScript)
frontend. No database yet — added in Slice 1.

## Tech stack

| Component       | Technology                  | Version      |
| --------------- | --------------------------- | ------------ |
| Backend         | Java, Spring Boot           | 25, 4.0.6    |
| Frontend        | TypeScript, React, Vite     | 6.0.2, 19.x, 8.x |
| Database        | PostgreSQL                  | 18.4 (not yet wired up) |
| Cache / session | Valkey                      | 9.1.0 (not yet wired up) |
| CI/CD           | GitHub Actions              | —            |
| Hosting         | Render.com                  | —            |
| Secrets         | Infisical                   | —            |
| Observability   | OpenTelemetry → Grafana Cloud | not yet set up (Slice 3) |
| Feature flags   | Flagsmith                   | not yet set up (Slice 5) |

See [ADR-001](decisions/001-use-valkey-over-redis.md) for why Valkey instead of Redis.
See [ADR-002](decisions/002-separate-frontend-images-per-environment.md) for why the frontend has separate images per environment.

## Backend structure

The backend follows hexagonal architecture with four layers:

- **`domain/`** — pure Java business logic. No Spring, no JPA, no framework
  annotations. Depends on nothing except `java.*` and `javax.*`.
- **`application/`** — use cases and service interfaces. Depends on `domain/`
  only. Defines repository interfaces (e.g. `ProductRepository`) that
  `infrastructure/` implements.
- **`infrastructure/`** — adapters: JPA repositories, external API clients,
  Spring config. May not import from `api/`.
- **`api/`** — REST controllers and DTOs. May not import from `infrastructure/`.

These rules are enforced automatically by ArchUnit tests in
`backend/src/test/java/com/secureshop/architecture/LayerDependencyTest.java`.

## Frontend structure

The frontend follows a layered module structure:

- **`api/`** — the only layer that makes HTTP calls. Typed fetch wrappers and
  response mapping live here.
- **`domain/`** — pure TypeScript: types, validation, business logic. No React
  imports.
- **`features/`** — self-contained feature modules. Features do not import from
  each other.
- **`shared/`** — reusable UI components and utilities shared across features.
- **`app/`** — app shell, routing, providers. Not yet used (Slice 0 has a single
  flat component).

These rules are enforced by `eslint-plugin-boundaries` configured in
`frontend/eslint.config.js`.

## Environments

| Environment | Backend URL                        | Frontend URL                       | Access           |
| ----------- | ---------------------------------- | ---------------------------------- | ---------------- |
| Development | http://localhost:8080              | http://localhost:5173              | Local only       |
| Staging     | set in `STAGING_URL` GitHub secret | same host, different service       | HTTP Basic Auth  |
| Production  | set in `PROD_URL` GitHub secret    | same host, different service       | Public           |

## Configuration and secrets

Non-secret configuration lives in committed YAML files:

- `backend/src/main/resources/application.yml` — shared defaults
- `backend/src/main/resources/application-dev.yml` — local dev overrides
- `backend/src/main/resources/application-staging.yml` — staging overrides
- `backend/src/main/resources/application-prod.yml` — production overrides

Secrets are never committed. They live in Infisical and are injected as
environment variables at deploy time via the `Infisical/secrets-action` step in
`deploy.yml`. See `runbooks/add-environment-variable.md` for the procedure.

## Database

Not yet in use. PostgreSQL 18.4 is defined in `docker-compose.yml` for local
development. Schema migrations via Flyway will be added in Slice 1.

## Deployment pipeline

1. Push or merge to `main`
2. `checks.yml` runs: formatting, tests, Docker build, Trivy scan
3. `deploy.yml` builds and pushes images to GHCR tagged with the commit SHA
4. Staging deploys automatically; health check must pass before proceeding
5. Manual approval gate (GitHub Environment: `production`)
6. Production deploys the same image SHA
7. Health check must pass

See `runbooks/deploy-to-production.md` for the manual procedure.

## Observability

Not yet set up. Planned for Slice 3: OpenTelemetry → Grafana Cloud for traces
and metrics, structured JSON logs via SLF4J + Logback.

Locally, Seq is available at http://localhost:8081 for log viewing once
`docker compose up` is running.

## Authentication

Not yet set up. Planned for Slice 4: Keycloak as the identity provider.
Spring Security will validate JWTs. The Keycloak admin console is available
locally at http://localhost:8180 once `docker compose up` is running.

## Feature flags

Not yet set up. Planned for Slice 5: Flagsmith.
