# ADR-002: Build Separate Frontend Docker Images per Environment

## Status: Accepted

## Context

The React frontend needs to know the backend API URL at runtime. Vite inlines
`VITE_*` environment variables into the static bundle at build time — there is
no mechanism to inject them at container startup without additional tooling.

Two approaches were considered:

1. **Nginx reverse proxy** — serve static files and proxy `/actuator/*` to the
   backend from within the same container, substituting the backend URL via
   `envsubst` at startup. No environment-specific images needed.

2. **Per-environment images** — bake `VITE_API_URL` into the bundle at build
   time, producing one image for staging and one for production.

The proxy approach was attempted first. It failed on Render because outbound
proxy requests from a container loop back through Cloudflare's CDN layer,
which detects repeated appearances of its own IPs in the `X-Forwarded-For`
chain and returns HTTP 508 Loop Detected. The backend never receives the
request.

## Decision

Build two frontend images per deploy:

- `secureshop-frontend-staging` — `VITE_API_URL` set to the staging backend URL
- `secureshop-frontend-prod` — `VITE_API_URL` set to the production backend URL

The browser calls the backend directly. CORS is configured on the backend per
Spring profile (`application-staging.yml`, `application-prod.yml`) to allow
requests from the respective frontend origin.

## Consequences

- The "one image, all environments" principle holds for the backend but not
  the frontend. This is an accepted trade-off for SPAs with build-time config.
- Each deploy builds two frontend images instead of one — minor CI cost.
- Changing the backend URL requires a frontend rebuild and redeploy.
- CORS must be kept in sync with the frontend origin in each profile YAML.
