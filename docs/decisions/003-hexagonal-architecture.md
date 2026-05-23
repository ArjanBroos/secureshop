# ADR-003: Use Hexagonal Architecture for the Backend

## Status: Accepted

## Context

The backend needs a structure that keeps business logic testable without a running
Spring context, and decoupled enough that the database or framework can be swapped
without touching domain code. The common alternative — a layered "controller →
service → repository" structure with Spring annotations throughout — tends to
bleed framework concerns into domain and service classes over time, making unit
tests slow and business logic hard to read in isolation.

## Decision

The backend uses hexagonal architecture (also called ports-and-adapters) with four
packages under `com.secureshop`:

- **`domain/`** — pure Java. No Spring, no JPA, no third-party annotations.
  Depends only on `java.*` and `javax.*`.
- **`application/`** — use cases and service interfaces. Depends only on
  `domain/`. Defines interfaces (e.g. `ProductRepository`) that `infrastructure/`
  implements.
- **`infrastructure/`** — adapters: JPA repositories, external clients, Spring
  config. May not import from `api/`.
- **`api/`** — REST controllers and DTOs. May not import from `infrastructure/`.

The dependency rule flows inward: `api` and `infrastructure` depend on
`application` and `domain`; `application` depends on `domain`; `domain` depends
on nothing.

These rules are enforced by ArchUnit tests in
`backend/src/test/java/com/secureshop/architecture/LayerDependencyTest.java`.
A violation fails the build — no discipline or code review vigilance required.

## Consequences

- More mapping code at layer boundaries (JPA entity ↔ domain model,
  DTO ↔ domain model). This is intentional: the boundaries are explicit rather
  than hidden.
- Domain and application logic is unit-testable with plain JUnit — no Spring
  context, no Testcontainers, no slow startup.
- Changing the database driver, ORM, or web framework does not touch business
  logic.
- New contributors need to understand the four-package rule before adding code.
  The ArchUnit tests surface violations immediately so the feedback loop is short.
