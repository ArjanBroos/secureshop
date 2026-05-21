# Add a New Environment Variable

## Is it a secret?

**Secrets** (credentials, API keys, tokens, passwords) and **non-secret config**
(log levels, feature toggles, URLs) are handled differently.

---

## Non-secret config (log levels, pagination sizes, CORS origins, etc.)

1. Add it to the appropriate committed YAML file:
   - `backend/src/main/resources/application.yml` — shared across all environments
   - `backend/src/main/resources/application-dev.yml` — local dev only
   - `backend/src/main/resources/application-staging.yml` — staging only
   - `backend/src/main/resources/application-prod.yml` — production only

2. Reference it in the backend via `@Value("${my.property}")` or a
   `@ConfigurationProperties` class.

3. Commit and deploy normally.

---

## Secrets

### 1. Add it to Infisical

Go to [Infisical](https://infisical.com) → your project → the appropriate
environment (dev, staging, or prod) and add the key/value pair.

### 2. Add it to `.env.example`

Add the key with an empty value and a comment explaining what it is and where
to get it. Never add the actual value.

```
# What this secret is for and where to obtain it
MY_SECRET_KEY=
```

### 3. Verify the deploy workflow fetches it

`deploy.yml` uses `Infisical/secrets-action` to inject secrets as environment
variables before each deploy step. Infisical secrets are automatically available
as env vars with the same name — no changes to the workflow file are needed
unless you're adding a secret to a new environment.

### 4. Reference it in the application

**Backend:** Spring Boot maps environment variables to properties automatically.
`MY_SECRET_KEY` becomes `my.secret-key` in YAML notation. Reference it with
`@Value("${my.secret-key}")` or in `application.yml` as
`my.secret-key: ${MY_SECRET_KEY}`.

**Frontend:** Prefix the key with `VITE_` (e.g. `VITE_MY_KEY`). Vite inlines
`VITE_*` variables at build time. Access them via `import.meta.env.VITE_MY_KEY`.
Note: frontend env vars are baked into the static bundle at build time, not at
runtime — they are not suitable for secrets.

### 5. Update `docs/architecture.md` if appropriate

If the variable meaningfully changes how the system is configured, note it in
the Configuration and secrets section.
