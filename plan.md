# Webshop Practice Project — Plan

The goal of this project is not to build a real webshop. It is to practice professional software delivery: environments, security, secrets, zero-downtime deploys, staging, observability, versioning, reliability, and clean architecture. The webshop is just a vehicle.

Everything is built in thin vertical slices — something small working fully from top to bottom, deployed to production, before expanding.

---

## Tech Stack

| Concern              | Tool                                                    |
| -------------------- | ------------------------------------------------------- |
| Backend              | Java 25, Spring Boot 4                                  |
| Frontend             | TypeScript, React (Vite)                                |
| Database             | PostgreSQL 18                                           |
| Local orchestration  | Docker Compose (infrastructure only, not the app)       |
| CI/CD                | GitHub Actions                                          |
| Container registry   | GitHub Container Registry (ghcr.io)                     |
| Hosting              | Render.com (free tier initially)                        |
| Secrets              | Infisical (free tier)                                   |
| Observability        | Serilog equivalent: SLF4J + Logback (structured JSON)   |
| Tracing / metrics    | OpenTelemetry → Grafana Cloud (free tier)               |
| Logging locally      | Seq (free, Docker)                                      |
| Auth                 | Keycloak (self-hosted in Docker locally, free tier cloud for staging/prod) |
| Feature flags        | Flagsmith (free tier)                                   |
| Dependency updates   | Dependabot (GitHub, free)                               |
| Secret scanning      | gitleaks (pre-commit + CI)                              |
| E2E tests            | Playwright                                              |
| Architecture tests   | ArchUnit (backend), eslint-plugin-import + eslint-plugin-boundaries (frontend) |

---

## Project Structure

```
/
├── .devcontainer/
│   ├── devcontainer.json     # Container definition, extensions, settings
│   └── docker-compose.yml    # Extends root docker-compose with devcontainer service
│
├── backend/
│   ├── src/main/java/com/shop/
│   │   ├── domain/           # Pure business logic, no framework annotations
│   │   │   └── model/        # Entities, value objects
│   │   ├── application/      # Use cases / services, orchestrates domain
│   │   │   ├── service/      # Use case implementations
│   │   │   └── repository/   # Interfaces for persistence (implemented by infrastructure)
│   │   ├── infrastructure/   # Adapters: DB repos, external APIs, messaging
│   │   │   ├── persistence/  # JPA entities, Spring Data repos implementing application interfaces
│   │   │   ├── config/       # Spring config, security, CORS, OpenTelemetry
│   │   │   └── external/     # Clients for third-party services
│   │   └── api/              # REST controllers, DTOs, request/response mapping
│   │       ├── controller/   # Uses application services and domain models freely
│   │       └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml                  # Shared defaults (no secrets)
│   │   ├── application-dev.yml              # Local dev overrides (committed, no secrets)
│   │   ├── application-staging.yml          # Staging overrides (committed, no secrets)
│   │   └── application-prod.yml             # Prod overrides (committed, no secrets)
│   ├── src/test/
│   │   ├── unit/             # Pure logic tests, no Spring context
│   │   └── integration/      # @SpringBootTest with Testcontainers
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw / mvnw.cmd          # Maven wrapper (pinned version, committed)
│
├── frontend/
│   ├── src/
│   │   ├── api/              # API client layer (fetch wrappers, types)
│   │   ├── domain/           # Business types, validation, pure logic
│   │   ├── features/         # Feature modules (each self-contained)
│   │   │   ├── catalog/      # Components + hooks + types for catalog
│   │   │   ├── cart/
│   │   │   └── checkout/
│   │   ├── shared/           # Shared UI components, hooks, utilities
│   │   └── app/              # App shell, routing, providers, layout
│   ├── e2e/                  # Playwright tests
│   ├── .env.example          # All keys, no values
│   ├── .prettierrc           # Prettier config (single source of truth for JS/TS/CSS formatting)
│   ├── Dockerfile
│   ├── vite.config.ts
│   └── package.json
│
├── docker-compose.yml        # Local infra: Postgres, Redis, Seq, Keycloak
├── docs/
│   ├── architecture.md       # Living doc: the system as it is RIGHT NOW
│   ├── decisions/            # ADRs: append-only, never delete, update status
│   │   └── 001-hexagonal-architecture.md
│   └── runbooks/
│       ├── local-dev-setup.md
│       ├── deploy-to-production.md
│       ├── rotate-secrets.md
│       ├── restore-database.md
│       └── add-environment-variable.md
├── .env.example              # Root-level keys documentation
├── .editorconfig
├── .gitignore
├── .gitleaks.toml
├── .husky/
│   └── pre-commit            # Runs formatting checks + gitleaks before every commit
├── .github/
    ├── workflows/
    │   ├── ci.yml            # PR checks
    │   └── deploy.yml        # Merge to main → staging → prod
    └── dependabot.yml
```

### Devcontainer

Every developer works inside a VS Code devcontainer. This eliminates all environment discrepancies — Java version, Node version, Docker-in-Docker, pre-commit hooks, editor extensions, and formatter settings are all defined in code and identical for everyone.

`.devcontainer/devcontainer.json`:

```jsonc
{
  "name": "Webshop Dev",
  "dockerComposeFile": ["../docker-compose.yml", "docker-compose.yml"],
  "service": "devcontainer",
  "workspaceFolder": "/workspace",

  "features": {
    "ghcr.io/devcontainers/features/java:1": { "version": "21", "installMaven": "true" },
    "ghcr.io/devcontainers/features/node:1": { "version": "20" },
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  },

  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-java-pack",
        "esbenp.prettier-vscode",
        "dbaeumer.vscode-eslint",
        "ms-playwright.playwright",
        "eamodio.gitlens"
      ],
      "settings": {
        // Java formatting — delegate to the Maven Spotless plugin
        "java.format.enabled": false,
        "[java]": {
          "editor.formatOnSave": true,
          "editor.defaultFormatter": "vscjava.vscode-java-pack"
        },
        // Frontend formatting — delegate to Prettier
        "[typescript]":      { "editor.formatOnSave": true, "editor.defaultFormatter": "esbenp.prettier-vscode" },
        "[typescriptreact]": { "editor.formatOnSave": true, "editor.defaultFormatter": "esbenp.prettier-vscode" },
        "[json]":            { "editor.formatOnSave": true, "editor.defaultFormatter": "esbenp.prettier-vscode" },
        "[css]":             { "editor.formatOnSave": true, "editor.defaultFormatter": "esbenp.prettier-vscode" },
        // ESLint auto-fix on save (catches boundary violations immediately)
        "editor.codeActionsOnSave": { "source.fixAll.eslint": "explicit" },
        // Consistent settings
        "files.trimTrailingWhitespace": true,
        "files.insertFinalNewline": true
      }
    }
  },

  "postCreateCommand": "cd frontend && npm ci && cd ../backend && ./mvnw dependency:resolve",
  "postStartCommand": "npx husky install"
}
```

`.devcontainer/docker-compose.yml` (extends the root compose file to add the dev container itself):

```yaml
services:
  devcontainer:
    image: mcr.microsoft.com/devcontainers/base:ubuntu
    volumes:
      - ..:/workspace:cached
    command: sleep infinity
    depends_on:
      - postgres
      - redis
      - seq
```

This means `docker compose up -d` for infrastructure happens automatically when the devcontainer starts. No manual step.

### Auto-Formatting

Formatting is handled by two tools, each owning its language:

**Backend — Spotless (Maven plugin):**

Add to `pom.xml`:

```xml
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <version>2.44.0</version>
  <configuration>
    <java>
      <googleJavaFormat>
        <version>1.25.2</version>
      </googleJavaFormat>
    </java>
  </configuration>
</plugin>
```

- `./mvnw spotless:apply` — formats all Java files
- `./mvnw spotless:check` — fails if any file is unformatted (used in CI)
- VS Code's format-on-save triggers the same Google Java Format style via the Java extension

**Frontend — Prettier + ESLint:**

`frontend/.prettierrc`:

```json
{
  "semi": true,
  "singleQuote": false,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2
}
```

- `npx prettier --write .` — formats all frontend files
- `npx prettier --check .` — fails if any file is unformatted (used in CI)
- VS Code's format-on-save triggers Prettier automatically via the extension configured in the devcontainer
- ESLint handles code quality rules (including boundary enforcement); Prettier handles style. They don't conflict because `eslint-config-prettier` disables ESLint rules that overlap with Prettier.

**Pre-commit hook — Husky + lint-staged:**

The pre-commit hook ensures nothing unformatted or leaking secrets ever gets committed, even if someone's editor didn't format on save:

`package.json` (root):

```json
{
  "devDependencies": {
    "husky": "^9.0.0",
    "lint-staged": "^15.0.0"
  },
  "lint-staged": {
    "backend/src/**/*.java": "cd backend && ./mvnw spotless:apply -q && git add",
    "frontend/src/**/*.{ts,tsx,css,json}": "prettier --write",
    "frontend/src/**/*.{ts,tsx}": "eslint --fix"
  }
}
```

`.husky/pre-commit`:

```bash
#!/bin/sh
npx lint-staged
npx gitleaks protect --staged
```

This runs on every commit:
1. **lint-staged** formats only the files being committed — Spotless for Java, Prettier for frontend, ESLint auto-fix for lint issues
2. **gitleaks** scans staged files for accidentally committed secrets

If either fails, the commit is rejected. The developer fixes the issue and commits again.

**CI as the final gate:**

The CI pipeline verifies formatting as a non-negotiable check. If someone bypasses the pre-commit hook (`git commit --no-verify`), CI catches it:

```
Backend:
  - ./mvnw spotless:check
  - ./mvnw verify

Frontend:
  - npx prettier --check .
  - npm run lint
  - npm run type-check
  - npm run test
```

The chain is: editor formats on save → pre-commit hook catches anything the editor missed → CI rejects anything the hook missed. Three layers, no formatting in code review ever.

### Backend Architecture — The Rules

The dependency rule is simple:

- **`domain`** depends on nothing except `java.*`. No Spring, no JPA, no framework annotations. Pure Java.
- **`application`** depends on `domain` and `java.*` only. No Spring, no JPA, no HTTP. It defines interfaces (e.g. `ProductRepository`) that infrastructure implements.
- **`api`** and **`infrastructure`** can depend on anything above them — domain, application, and frameworks.

Concretely:

- `domain/model/` contains plain Java classes. Your business objects, value objects, and any logic that lives on them.
- `application/service/` contains use case implementations. They orchestrate domain objects and call repository interfaces. Annotated with `@Service` for Spring DI is fine — that's the one concession, and it's optional if you wire beans manually in a config class.
- `application/repository/` (or whatever you want to call it) contains interfaces like `ProductRepository`, `PaymentGateway`. These are plain Java interfaces — no Spring Data, no `@Repository`. Infrastructure provides the implementations.
- `infrastructure/persistence/` contains JPA entities (separate from domain models), Spring Data repositories, and mappers. A class here implements `application/repository/ProductRepository` by wrapping a Spring Data `JpaRepository` and mapping between JPA entities and domain models.
- `api/controller/` contains REST controllers. They call application services, work with domain models, and convert to/from DTOs. Controllers can use both application and domain freely — the DTO ↔ domain mapping happens here.

Mapping happens at the boundaries: API DTOs ↔ domain models (in controllers), domain models ↔ JPA entities (in persistence adapters).

The rule to enforce: if a class is in `domain/` or `application/`, and its imports include anything from `org.springframework`, `jakarta.persistence`, or any other framework — it's wrong. That's the only rule that matters. Everything else follows from it.

### Frontend Architecture — The Rules

- `api/` is the only place that talks to the backend. Every HTTP call lives here with typed request/response interfaces. Nothing else in the app uses `fetch` directly.
- `domain/` contains pure TypeScript: types, validation functions, business logic. No React imports.
- `features/` is organized by business capability. Each feature folder is self-contained: its own components, hooks, and local types. Features don't import from each other — if they need to share, it moves to `shared/`.
- `shared/` contains reusable UI components (Button, Modal, Layout), shared hooks, and utilities.
- `app/` is the shell: routing, global providers, layout. Thin as possible.

---

## Environment Configuration

### Profiles

| Environment | `SPRING_PROFILES_ACTIVE` | `VITE_APP_ENV` | Where it runs |
| ----------- | ------------------------ | -------------- | ------------- |
| Development | `dev`                    | `development`  | Your laptop   |
| Staging     | `staging`                | `staging`      | Render (protected by HTTP Basic Auth) |
| Production  | `prod`                   | `production`   | Render        |

### What goes where

| Value                                    | Where it lives                                      |
| ---------------------------------------- | --------------------------------------------------- |
| Log level, default pagination size       | `application.yml` (committed, shared defaults)       |
| Local Postgres URL (localhost:5432)       | `application-dev.yml` (committed, not secret)        |
| Staging log level tweaks                 | `application-staging.yml` (committed)                |
| Prod CORS allowed origins                | `application-prod.yml` (committed)                   |
| Local Postgres password (`localdevpw`)   | `application-dev.yml` (matches docker-compose, not actually secret) |
| Staging/prod DB connection string        | Infisical → injected as env var at deploy time       |
| OAuth client secrets                     | Infisical → injected as env var at deploy time       |
| Stripe keys                              | Infisical → injected as env var at deploy time       |
| Frontend staging/prod API URL            | Build arg from CI, baked into static bundle          |

Spring Boot automatically picks up environment variables. An env var like `SPRING_DATASOURCE_URL` overrides `spring.datasource.url` in any YAML file. Secrets never appear in committed files — they override via environment variables injected at container start.

### One Docker image, all environments

The same image is deployed to staging and production. The image contains all `application-*.yml` files (they hold no secrets). Behaviour changes entirely based on runtime environment variables: `SPRING_PROFILES_ACTIVE=prod` plus injected secrets.

For the React frontend: the build needs the API URL at build time (Vite inlines `VITE_*` variables). So CI builds two bundles — one for staging, one for production — or uses a runtime config pattern where the HTML shell fetches `/config.json` at startup.

---

## Slices

Each slice is a full vertical: domain model → application service → persistence adapter → API endpoint → frontend feature → deployed to staging and production.

### Testing Standard

Every slice that touches code must include tests from both backend and frontend. Here is what is expected per layer:

**Architecture tests (set up in Slice 0, run on every PR from then on):**

These tests enforce the layer dependency rules automatically. They run in CI and fail the build if someone introduces a forbidden dependency — no code review vigilance required.

Backend — **ArchUnit** (add `com.tngtech.archunit:archunit-junit5` to `pom.xml`):

```java
// test/architecture/LayerDependencyTest.java

@AnalyzeClasses(packages = "com.shop")
class LayerDependencyTest {

    @ArchTest
    static final ArchRule domain_depends_on_nothing_except_java =
        classes().that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat(
                resideInAnyPackage("..domain..", "java..", "javax..")
            );

    @ArchTest
    static final ArchRule application_depends_only_on_domain_and_java =
        classes().that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat(
                resideInAnyPackage("..application..", "..domain..", "java..", "javax..")
            );

    @ArchTest
    static final ArchRule infrastructure_does_not_depend_on_api =
        noClasses().that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat()
            .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule api_does_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..api..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");
}
```

These four rules encode the entire backend architecture. If someone adds a JPA annotation to a domain class, the build breaks. If an application service imports a Spring Data repository directly instead of its own interface, the build breaks. The architecture is enforced by the compiler, not by discipline.

Extend these as the codebase grows. Useful additions later:
- Controllers must not return domain entities directly (must use DTOs)
- No field injection (`@Autowired` on fields) — constructor injection only
- Domain model classes must not be annotated with `@Entity`, `@Table`, etc.

Frontend — **eslint-plugin-boundaries** (add to `.eslintrc`):

```json
{
  "plugins": ["boundaries"],
  "settings": {
    "boundaries/elements": [
      { "type": "domain",  "pattern": "src/domain/*" },
      { "type": "api",     "pattern": "src/api/*" },
      { "type": "features","pattern": "src/features/*" },
      { "type": "shared",  "pattern": "src/shared/*" },
      { "type": "app",     "pattern": "src/app/*" }
    ]
  },
  "rules": {
    "boundaries/element-types": [2, {
      "default": "disallow",
      "rules": [
        { "from": "domain",   "allow": [] },
        { "from": "api",      "allow": ["domain"] },
        { "from": "shared",   "allow": [] },
        { "from": "features", "allow": ["domain", "api", "shared"] },
        { "from": "app",      "allow": ["domain", "api", "shared", "features"] }
      ]
    }],
    "boundaries/no-unknown": [2],
    "boundaries/no-unknown-files": [2]
  }
}
```

This enforces: domain imports nothing app-specific, api only imports from domain, features don't import from each other, shared is self-contained. A developer adding a React hook to `domain/` or importing `features/cart` from `features/checkout` gets a lint error immediately in their editor, not in code review.

Both of these run as part of the normal CI pipeline — ArchUnit runs with `./mvnw verify`, eslint-plugin-boundaries runs with `npm run lint`. No extra CI steps needed.

**Backend tests (every slice with backend changes):**
- Unit tests for domain logic (plain JUnit, no Spring context)
- Integration tests for persistence and API (Testcontainers + `@SpringBootTest` / MockMvc)

**Frontend tests (every slice with frontend changes):**
- **Domain tests:** Every pure function in `frontend/src/domain/` gets Vitest tests. Business logic like cart totals, validation rules, price calculations — all tested with plain input/output, no React, no mocking.
- **API layer tests:** Every function in `frontend/src/api/` gets Vitest tests with `fetch` mocked. Verify that backend DTO formats (snake_case, nested objects, etc.) map correctly to frontend domain types. Verify error handling (non-200 responses throw, network errors are caught). These are the tests that catch silent breakage when the backend changes a field name.
- **Component tests:** Each feature's main page component gets at least two React Testing Library tests: one for the happy path (data loads, user sees the right content) and one for the error state (API fails, user sees a meaningful message). Mock at the `api/` boundary, not at `fetch`. Test user-visible behaviour, not implementation details — no snapshot tests, no asserting on CSS classes or internal state.
- **Hook tests:** Only for hooks with meaningful logic (pagination, debounced search, complex form state). Use `renderHook` from React Testing Library. Simple hooks that just wrap a single API call are tested through the component instead.
- **E2E tests (Playwright):** Added in Slice 2 and expanded with each new feature slice. Tagged `@smoke` tests run in the deploy pipeline after staging deploy.

When a slice says "Tests:" below, it lists both backend and frontend expectations explicitly.

### Slice 0 — The Skeleton (no business logic)

**What you build:**
- VS Code devcontainer with Java 21, Node 20, Docker-in-Docker, and all extensions pre-configured
- Spring Boot app with health check endpoint (`/actuator/health`)
- React app with a single page that says "Shop coming soon" and calls the health endpoint
- Docker Compose with Postgres and Seq
- Dockerfiles for both backend and frontend
- Auto-formatting: Spotless (backend), Prettier (frontend), format-on-save in VS Code, Husky pre-commit hook
- GitHub Actions CI: build, test, lint, format check
- GitHub Actions deploy: build images, push to GHCR, deploy to Render (staging), smoke test, manual gate, deploy to Render (production)
- Infisical project with dev/staging/prod environments
- gitleaks pre-commit hook and CI check
- `.editorconfig`, `.gitignore`, Maven wrapper pinning (`mvnw`)
- Staging protected by HTTP Basic Auth on Render
- ArchUnit architecture tests (backend) — even with no domain code yet, set up the test class so rules are enforced from the first line of business logic onward
- eslint-plugin-boundaries configuration (frontend) — same principle, rules in place before features exist

**Tests:**
- Backend: health endpoint returns 200 (MockMvc)
- Frontend: API layer test that the health check client handles success and failure responses. Component test that the landing page renders without crashing.

**What you prove:**
- The full pipeline works end-to-end with zero business logic in the way
- Secrets are injected, never committed
- Staging is protected, production is public
- Zero-downtime deploy works (Render's health check integration)
- Structured JSON logs appear in Seq locally and Render's log viewer in staging/prod

**This is the most important slice. Do not move on until everything here works reliably.**

### Slice 1 — View Products (read-only)

**Domain:** `Product` (id, name, description, price, imageUrl)
**Application:** `ViewCatalogUseCase` — fetches products, supports pagination
**Infrastructure:** `JpaProductRepository` implementing `ProductRepository` port, JPA entity with mapping
**API:** `GET /api/v1/products`, `GET /api/v1/products/{id}` — returns DTOs
**Frontend:** `features/catalog/` — product list page, product detail page, API client in `api/products.ts`

**Tests:**
- Backend: unit test for any domain logic, integration test for repository with Testcontainers, API test with MockMvc
- Frontend domain: if any pure formatting or logic exists (e.g. price display helpers), test it
- Frontend API layer: test that `getProducts()` maps the backend DTO (snake_case fields) to the `Product` domain type correctly, test error handling for failed requests
- Frontend component: CatalogPage shows product names after loading, CatalogPage shows error state when API fails

**What you prove:**
- Clean separation actually works: domain model has no JPA annotations, mapping happens at boundaries
- API versioning (`/v1/`) is in place from the start
- Frontend feature module pattern works
- Frontend API mapping is tested — a backend field rename will break a test, not production
- Database migration runs via Flyway on deploy

### Slice 2 — E2E Tests and Smoke Tests

**Set up Playwright and write the first end-to-end tests, run against the live staging environment in the deploy pipeline.**

**Playwright setup:**
- Install Playwright in `frontend/e2e/`
- Configure it to run against a configurable base URL via environment variable
- Add a `test:e2e` script to `frontend/package.json`
- In `deploy.yml`: after the staging health check, add a step that runs `@smoke`-tagged Playwright tests against the staging URL — staging must pass E2E before production promotion

**Tests:**
- Browse the catalog page — products load, names are visible (`@smoke`)
- (Seed data required in staging: at least one product in the database)

**What you prove:**
- Playwright is wired up and runs against the real staging environment after every deploy
- You have automated confidence that the catalog works end-to-end before production promotion
- Smoke tests are fast (under 2 minutes) and reliable

---

### Slice 3 — Add to Cart

**Domain:** `Cart`, `CartItem` (value objects, business rules like "quantity must be > 0", "can't add same product twice")
**Application:** `ManageCartUseCase` — add item, remove item, view cart
**Infrastructure:** Cart stored in HTTP session or Redis (your choice — Redis is more realistic)
**API:** `POST /api/v1/cart/items`, `DELETE /api/v1/cart/items/{productId}`, `GET /api/v1/cart`
**Frontend:** `features/cart/` — cart sidebar/page, add-to-cart button on product cards, cart state store (Zustand or similar)

**Tests:**
- Backend: unit tests for cart business rules (rich — test edge cases in pure domain logic), integration test for cart persistence
- Frontend domain: test `cartTotal()`, `canCheckout()`, `validateQuantity()` — all pure functions, many edge cases (empty cart, zero quantity, max quantity)
- Frontend API layer: test cart API functions map DTOs correctly, test error handling
- Frontend component: CartPage shows items and total, CartPage shows empty state, adding an item updates the displayed count
- Frontend hook/store: if using Zustand, test the cart store directly — adding items, removing items, quantity updates

**What you prove:**
- Domain logic with real business rules lives in `domain/`, tested without Spring (backend) and without React (frontend)
- State management pattern in frontend is established

### Slice 4 — Observability

**Backend:** Add OpenTelemetry SDK with tracing (Spring Web, JDBC, HTTP client instrumentation) and metrics, export to Grafana Cloud OTLP endpoint
**Frontend:** Add basic error tracking (window.onerror → POST to a backend error endpoint, or use Sentry free tier)
**Dashboard:** Create a basic Grafana dashboard with request rate, error rate, response time (the RED method)
**Health checks:** Expand `/actuator/health` to include Postgres connectivity, Redis connectivity, downstream services

**Tests:**
- Backend: verify health endpoint returns degraded/down status when dependencies are unavailable (Testcontainers — stop the database, check the response)
- Frontend API layer: test that the error reporting client sends the right payload format
- No new component tests needed — this slice is infrastructure, not UI

**What you prove:**
- A request can be traced from frontend → API → database and back in Grafana
- You can find a slow request and see exactly where the time was spent
- Health checks actually reflect real readiness (not just "the JVM started")

### Slice 5 — Authentication

**Backend:** Integrate Keycloak as the identity provider. Spring Security validates JWTs from Keycloak. Protect write endpoints (cart, checkout). Leave catalog endpoints public.
**Frontend:** Keycloak JS adapter for login/logout. Auth token attached to API requests via an interceptor in the `api/` client layer. Unauthenticated users can browse but not add to cart.
**Security headers:** Add CSP, HSTS, X-Frame-Options, X-Content-Type-Options via a Spring filter/middleware

**Tests:**
- Backend: MockMvc tests verifying protected endpoints return 401/403 without a valid token, and succeed with one
- Frontend API layer: test that the request client attaches the auth header when a token is present, and omits it when not
- Frontend component: test that the "Add to cart" button is hidden or disabled when the user is not authenticated, test that it appears when authenticated (mock the auth context/hook)

**What you prove:**
- Auth is handled by a dedicated identity provider, not custom code
- Tokens flow correctly through the system
- Security headers are present on every response
- CORS is properly configured per environment

### Slice 6 — Checkout (with feature flag)

**Domain:** `Order`, `OrderItem`, `OrderStatus` (PENDING → CONFIRMED → SHIPPED)
**Application:** `PlaceOrderUseCase` — validates cart, creates order, clears cart
**Infrastructure:** `JpaOrderRepository`, Flyway migration for orders table
**API:** `POST /api/v1/orders` (authenticated), `GET /api/v1/orders/{id}`
**Frontend:** `features/checkout/` — checkout page, order confirmation page
**Feature flag:** The entire checkout feature is behind a Flagsmith flag. Enabled in staging for testing, disabled in production initially. When ready: enable for production. Then remove the flag and the old code path.

**Tests:**
- Backend: unit test for order validation logic, integration test for order persistence, MockMvc test for the full order creation flow
- Frontend domain: test order validation (e.g. `canCheckout` with empty cart returns false)
- Frontend API layer: test order submission maps correctly, test error handling for failed orders
- Frontend component: CheckoutPage renders the cart summary and submit button, CheckoutPage shows confirmation after successful order, CheckoutPage shows error on failure, checkout route/button is hidden when feature flag is off (mock Flagsmith)

**What you prove:**
- Feature flag workflow: ship to production with the feature hidden, verify in staging, enable gradually, retire the flag
- The expand/contract migration pattern if the orders table needs adjusting after initial release
- End-to-end order flow works with auth

### Slice 7 — Zero-Downtime Database Migration

**Goal:** Practice the expand/contract migration pattern.

**Scenario:** Rename `Product.description` to `Product.details` (or split a column, change a type — pick something that requires the pattern).

**Steps:**
1. Deploy 1: Flyway migration adds new column, backfills data, app writes to both columns, reads from new
2. Deploy 2: App stops writing to old column
3. Deploy 3: Flyway migration drops old column

**What you prove:**
- You can make schema changes without downtime
- You understand why this is three deploys, not one

### Slice 8 — Secret Rotation Drill

**Goal:** Rotate the production database password with zero downtime.

**Steps:**
1. Create a second database user with identical permissions
2. Update the secret in Infisical to point to the new user
3. Deploy — new containers pick up new credentials, old containers drain naturally
4. Verify health checks pass
5. Drop the old database user

**What you prove:**
- Credential rotation without downtime is a practiced, understood procedure

### Slice 9 — Backup and Restore Drill

**Goal:** Prove your data is recoverable.

**Steps:**
1. Set up automated `pg_dump` via GitHub Actions scheduled workflow (weekly)
2. Upload dump to Backblaze B2 (free tier)
3. Spin up a blank Postgres container locally
4. Restore the dump
5. Verify data integrity

**What you prove:**
- Backups exist and are automated
- Restoration actually works

---

## Security Checklist (From Day One)

These are set up in Slice 0 and maintained throughout:

- [ ] gitleaks runs as pre-commit hook and in CI
- [ ] No secrets in any committed file (verify with `git log --all -p | grep -i password`)
- [ ] Dockerfiles use a non-root user (`USER 1000` or named user)
- [ ] `docker-compose.yml` pins image versions (`postgres:16.2` not `postgres:latest`)
- [ ] `.dockerignore` excludes `.env`, `.env.local`, `secrets/`, `*.pem`
- [ ] HTTPS enforced (Render handles TLS termination, but verify HSTS header is set)
- [ ] CORS locked to specific origins per environment (from config, not hardcoded)
- [ ] `Content-Security-Policy` header present and as strict as your frontend allows
- [ ] `X-Content-Type-Options: nosniff` header present
- [ ] `X-Frame-Options: DENY` header present
- [ ] Dependabot enabled for Maven, npm, Docker, and GitHub Actions
- [ ] `./mvnw org.owasp:dependency-check-maven:check` (OWASP dependency-check) runs in CI
- [ ] Docker images scanned with Trivy in CI
- [ ] Input validation on all API endpoints (Bean Validation / `@Valid`)
- [ ] Parameterised queries only (JPA/Hibernate does this by default — never concatenate SQL)
- [ ] Rate limiting on authentication endpoints at minimum

---

## CI/CD Pipeline Detail

### `ci.yml` — runs on every PR

```
Trigger: pull_request to main

Steps:
  1. Checkout code
  2. Set up Java 21, Node 20
  3. gitleaks scan
  4. Backend:
     - ./mvnw spotless:check (reject unformatted code)
     - ./mvnw verify (compiles + unit tests + integration tests via Failsafe)
     - ./mvnw org.owasp:dependency-check-maven:check
  5. Frontend:
     - npm ci
     - npx prettier --check . (reject unformatted code)
     - npm run lint (includes eslint-plugin-boundaries architecture rules)
     - npm run type-check
     - npm run test (Vitest unit tests)
  6. Build Docker images (verify they build, don't push)
  7. Trivy scan both images
```

### `deploy.yml` — runs on merge to main

```
Trigger: push to main

Steps:
  1. Everything from ci.yml
  2. Build and push Docker images to ghcr.io (tagged with commit SHA)
  3. Fetch secrets from Infisical for staging
  4. Deploy commit SHA to Render staging
  5. Wait for health checks to pass
  6. Run Playwright smoke tests against staging URL
  7. ── Manual approval gate (GitHub Environment protection rule) ──
  8. Fetch secrets from Infisical for production
  9. Deploy same commit SHA to Render production
  10. Wait for health checks to pass
  11. Run Playwright smoke tests against production URL (optional, 2-3 critical paths only)
```

---

## What NOT to Do

- Don't set up Prometheus/Grafana/Tempo as self-hosted Docker containers. Use Grafana Cloud free tier. You are not here to practice sysadmin.
- Don't build a real payment integration. A fake `PlaceOrderUseCase` that always succeeds is fine. The payment is not the point.
- Don't spend time on UI polish until the pipeline and architecture are solid. A functional but ugly checkout page that deploys cleanly to production with proper observability is worth more than a beautiful page deployed manually.
- Don't add complexity before you need it. No message queues, no event sourcing, no CQRS. Add them in a future slice if you want to practice those patterns.
- Don't flag every feature. Only Slice 5 uses a feature flag, to practice the workflow. The other slices ship directly.
- Don't skip the drills (Slices 7, 8, 9). The operational practices are the entire point of this project. A webshop that can't survive a database password rotation is not production-ready, no matter how clean the code is.

---

## Definition of Done (per slice)

A slice is done when:

1. Code compiles and all tests pass locally
2. CI pipeline passes on the PR
3. Code is reviewed (or self-reviewed with a checklist if solo)
4. Merged to main
5. Automatically deployed to staging
6. Smoke tests pass in staging
7. Manually promoted to production
8. Verified working in production
9. No secrets in source control (gitleaks confirms)
10. Health checks pass in all environments
11. Structured logs visible in Grafana Cloud (from Slice 3 onward)
12. `docs/architecture.md` is updated to reflect any changes to the system's current state
13. Any new operational procedures have a corresponding runbook in `docs/runbooks/`
14. Any non-obvious decisions are recorded as ADRs in `docs/decisions/`

---

## Documentation

Three kinds of documentation live in `/docs/`, each serving a different purpose:

- **`architecture.md`** tells you **what** the system looks like now. A new developer reads this first.
- **`decisions/`** (ADRs) tell you **why** it looks that way. You read these when something seems odd.
- **`runbooks/`** tell you **how** to operate it. You follow these when performing a task.

### `architecture.md` — The Living Document

This file describes the system as it currently is. It is updated with every slice — if a slice changes how something works, the architecture doc is updated in the same PR. It is not a history, it is the current truth.

It should contain the following sections. Below is the starting skeleton — fill it in during Slice 0 and keep it current from there:

```markdown
# Architecture

Last updated: [date, updated with every change]

## Overview

A webshop consisting of a Java Spring Boot backend API, a React
(TypeScript) frontend, and a PostgreSQL database.

## Tech stack

| Component        | Technology                  | Version |
| ---------------- | --------------------------- | ------- |
| Backend          | Java, Spring Boot           | 25, 4.x |
| Frontend         | TypeScript, React, Vite     | 5.x, 18.x |
| Database         | PostgreSQL                  | 18.4    |
| Identity         | Keycloak                    | x.x     |
| CI/CD            | GitHub Actions              | —       |
| Hosting          | Render.com                  | —       |
| Secrets          | Infisical                   | —       |
| Observability    | OpenTelemetry → Grafana Cloud | —     |
| Feature flags    | Flagsmith                   | —       |

## Backend structure

[Describe the layer rules: domain depends on nothing, application
depends on domain only, api and infrastructure depend on both.
Point to the relevant ADR for the reasoning.]

## Frontend structure

[Describe the layer rules: domain has no React imports, api/ is
the only layer that talks to the backend, features don't import
from each other. Point to the relevant ADR.]

## Environments

| Environment | URL                              | Access             |
| ----------- | -------------------------------- | ------------------ |
| Development | http://localhost:8080 (backend)  | Local only         |
|             | http://localhost:5173 (frontend) |                    |
| Staging     | https://staging.myapp.onrender.com | HTTP Basic Auth  |
| Production  | https://myapp.onrender.com       | Public             |

## Configuration & secrets

[Explain: application-*.yml files hold non-secret config per
environment. Real secrets are injected via environment variables
from Infisical. Refer to runbooks/add-environment-variable.md
for the procedure.]

## Database

[Current schema overview. Which tables exist, what Flyway
migration version we're on. Updated when migrations are added.]

## Deployment pipeline

[Describe the current flow: PR → CI → merge → staging deploy →
smoke tests → manual gate → production deploy. Refer to
runbooks/deploy-to-production.md for the procedure.]

## Observability

[Where to find logs, traces, and metrics. Grafana Cloud
dashboard URL. What the health check endpoints return.]

## Authentication

[How auth works: Keycloak issues JWTs, backend validates them,
frontend attaches them. Which endpoints are protected, which
are public.]

## Feature flags

[Which flags currently exist, what they control, which
environments they're enabled in. Remove entries when flags
are retired.]
```

The discipline: if a PR changes something described in `architecture.md` and doesn't update the doc, the PR is not ready to merge.

### Architecture Decision Records

Keep ADRs in `/docs/decisions/`. Append-only — never delete or substantially edit an existing ADR. Only update the **Status** field when a decision is superseded or deprecated.

Status values:
- **Accepted** — this is the current approach
- **Superseded by ADR-XXX** — replaced by a later decision (link to it)
- **Deprecated** — no longer relevant, kept for history

Template:

```markdown
# ADR-001: Use Hexagonal Architecture for Backend

## Status: Accepted

## Context
We need a backend architecture that keeps business logic testable
and independent of frameworks.

## Decision
Domain and application layers have no Spring dependencies.
Infrastructure implements application-defined interfaces.

## Consequences
- More mapping code at boundaries (JPA entity ↔ domain model,
  DTO ↔ domain model)
- Domain logic is trivially unit-testable
- Changing the database or framework doesn't touch business logic
- New developers need to understand the layer rules
```

When a new decision supersedes an old one, update the old one:

```markdown
## Status: Superseded by ADR-015
```

And in the new ADR:

```markdown
## Context
ADR-001 established hexagonal architecture. After 6 months we
found that [reason]. This supersedes ADR-001.
```

Write an ADR for every non-obvious choice: why Keycloak over Auth0, why Flyway over Liquibase, why Render over a VPS, why Maven over Gradle, etc.

### Runbooks

Runbooks live in `/docs/runbooks/`. Each is a step-by-step procedure that someone can follow without additional context. They are updated whenever the procedure changes.

The following runbooks should be created during the indicated slices:

**`local-dev-setup.md`** — Created in Slice 0. What a new developer does on day one:

```markdown
# Local Development Setup

## Prerequisites
- VS Code
- Docker Desktop (or Podman)
- VS Code "Dev Containers" extension (ms-vscode-remote.remote-containers)

That's it. Java, Node, and all other tools are provided by the devcontainer.

## Steps

1. Clone the repo
   git clone git@github.com:yourorg/webshop.git
   cd webshop

2. Open in VS Code
   code .
   VS Code will detect .devcontainer/ and prompt:
   "Reopen in Container" — click it.
   First build takes a few minutes (pulls images, installs dependencies).
   Subsequent opens are fast.

3. Verify infrastructure is running
   The devcontainer starts Postgres, Redis, Seq, and Keycloak automatically.
   docker compose ps — all services should show "Up"
   Seq dashboard: http://localhost:8081
   Keycloak admin: http://localhost:8180

4. Set up backend secrets
   Copy backend/.env.example to backend/.env.local
   Fill in shared dev values from Infisical:
     - KEYCLOAK_CLIENT_SECRET — get from Infisical "dev" environment
   Values marked "generate your own" need no action for local dev.

5. Run the backend
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

6. Set up frontend
   cd frontend
   cp .env.example .env.local
   Fill in:
     - VITE_API_URL=http://localhost:8080
   npm run dev

7. Verify everything works
   Frontend: http://localhost:5173
   Backend health: http://localhost:8080/actuator/health
   (should return {"status":"UP"})

8. Verify formatting works
   Edit any Java file and save — it should auto-format
   Edit any .tsx file and save — it should auto-format
   Try committing unformatted code — the pre-commit hook should reject it

## Troubleshooting

**Devcontainer fails to build**
  Check Docker Desktop is running and has enough resources (4GB+ RAM)

**Port already in use**
  docker compose down, then reopen in container

**Backend can't connect to Postgres**
  Verify Postgres is running: docker compose ps
  The devcontainer networking should handle this — services are on
  the same Docker network

**Pre-commit hook not running**
  Run: npx husky install

**Formatting not working on save**
  Check VS Code bottom-right status bar for the active formatter
  Ensure the extensions from devcontainer.json are installed
```

**`deploy-to-production.md`** — Created in Slice 0, expanded as the pipeline matures:

```markdown
# Deploy to Production

## Normal flow (automated)

1. Merge PR to main
2. CI runs: build, test, lint, security scans
3. Docker images built and pushed to ghcr.io (tagged with commit SHA)
4. Staging deploys automatically
5. Smoke tests run against staging
6. Go to GitHub Actions → deploy workflow → review the pending approval
7. Click "Approve and deploy"
8. Production deploys the same image
9. Verify: check production URL, check Grafana dashboard, check health endpoint

## Emergency rollback

1. Go to Render dashboard
2. Select the service (backend or frontend)
3. Under "Deploys", find the last known good deploy
4. Click "Redeploy"
5. Wait for health checks to pass
6. Investigate the issue on a branch, not in production

## Manual deploy (if CI is down)

1. Build locally:
   cd backend && ./mvnw package -DskipTests
   docker build -t ghcr.io/yourorg/webshop-api:manual .
   docker push ghcr.io/yourorg/webshop-api:manual
2. In Render dashboard, trigger a manual deploy with the image tag
3. This is an emergency procedure — file an issue to fix CI immediately
```

**`rotate-secrets.md`** — Created in Slice 8:

```markdown
# Rotate Database Credentials

## When to use
Scheduled rotation (quarterly) or suspected credential compromise.

## Steps

1. Create a new database user with identical permissions
   psql: CREATE USER myapp_b WITH PASSWORD 'newpassword';
   GRANT ALL ON ALL TABLES IN SCHEMA public TO myapp_b;

2. Update the secret in Infisical
   Environment: production
   Key: SPRING_DATASOURCE_URL
   Update the username and password in the connection string

3. Deploy
   Merge any pending PR, or trigger a redeploy of the current image
   New containers will pick up the new credentials
   Old containers continue working with old credentials until they drain

4. Verify
   Check /actuator/health — database check should be UP
   Check Grafana for any connection errors
   Wait 10 minutes to confirm stability

5. Remove old credentials
   psql: REVOKE ALL ON ALL TABLES IN SCHEMA public FROM myapp_a;
   DROP USER myapp_a;

6. Update Infisical
   Remove the old credential entry if stored separately
   Add a note in the secret's description: "Rotated [date]"
```

**`restore-database.md`** — Created in Slice 9:

```markdown
# Restore Database from Backup

## When to use
Data corruption, accidental deletion, or disaster recovery.

## Locate the backup

Automated backups: Backblaze B2 bucket "webshop-backups"
  Weekly pg_dump files named: webshop-YYYY-MM-DD.sql.gz
Render automated backups: Render dashboard → Database → Backups

## Restore to a local database (for verification)

1. Download the backup
   b2 download-file webshop-backups webshop-2026-05-18.sql.gz ./backup.sql.gz
   gunzip backup.sql.gz

2. Start a clean local Postgres
   docker run --name restore-test -e POSTGRES_PASSWORD=test -p 5433:5432 -d postgres:16.2

3. Restore
   psql -h localhost -p 5433 -U postgres -f backup.sql

4. Verify
   psql -h localhost -p 5433 -U postgres -d myapp -c "SELECT count(*) FROM products;"
   Compare row counts with what you expect

5. Clean up
   docker stop restore-test && docker rm restore-test

## Restore to production

⚠️  This replaces all production data. Confirm with the team first.

1. Put the site into maintenance mode (or accept brief downtime)
2. In Render dashboard: Database → Backups → select the backup → Restore
   OR use psql to restore from B2 backup to the Render database
3. Verify the application health checks pass
4. Check critical data: recent orders, user accounts
5. Monitor Grafana for errors for 30 minutes
```

**`add-environment-variable.md`** — Created in Slice 0:

```markdown
# Add a New Environment Variable

## Steps

1. Add the key to the appropriate .env.example file with a comment
   explaining what it's for and where to get the value

2. If it's a secret (credentials, API keys, tokens):
   a. Add it to Infisical for each environment (dev, staging, prod)
   b. Update the GitHub Actions deploy workflow to fetch it
   c. Add it to the Render service's environment variables
   d. Do NOT put the value in any committed file

3. If it's non-secret config (log level, feature toggle, URL):
   a. Add it to the appropriate application-{env}.yml file
   b. Commit it normally

4. Update docs/architecture.md if it changes how config works

5. In the backend, reference it via Spring's @Value or
   configuration properties. In the frontend, prefix with VITE_
   and access via import.meta.env.VITE_KEY_NAME

6. Test locally before pushing:
   - Backend: verify the app starts with the new variable
   - Frontend: verify Vite exposes it correctly
```

Additional runbooks to add as the project matures:
- `add-new-feature-flag.md` — created in Slice 5
- `respond-to-cve.md` — created when the first Dependabot security alert arrives
- `debug-production-issue.md` — created in Slice 3 alongside observability setup
