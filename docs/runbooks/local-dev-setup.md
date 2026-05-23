# Local Development Setup

## Prerequisites

- VS Code
- Docker Desktop (or Podman)
- VS Code "Dev Containers" extension (`ms-vscode-remote.remote-containers`)

That's it. Java, Node, and all other tools are provided by the devcontainer.

## Steps

1. Clone the repo
   ```
   git clone git@github.com:yourorg/webshop.git
   cd webshop
   ```

2. Open in VS Code
   ```
   code .
   ```
   VS Code will detect `.devcontainer/` and prompt "Reopen in Container" — click it.
   First build takes a few minutes (pulls the image, installs dependencies).
   Subsequent opens are fast.

3. Start the infrastructure
   From a terminal inside the devcontainer:
   ```
   docker compose up -d
   ```
   This starts Postgres, Valkey, Seq, and Keycloak.
   ```
   docker compose ps
   ```
   All services should show "Up".
   - Seq dashboard: http://localhost:8081
   - Keycloak admin: http://localhost:8180 (admin / admin)

4. Run the backend
   ```
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. Run the frontend
   ```
   cd frontend
   npm run dev
   ```
   `VITE_API_URL` is left empty in `.env.example`; Vite's proxy routes
   `/actuator/*` to `localhost:8080` so no `.env.local` edit is needed.

6. Verify everything works
   - Frontend: http://localhost:5173 — should show "Shop coming soon" with backend status UP
   - Backend health: http://localhost:8080/actuator/health — should return `{"status":"UP"}`

7. Verify formatting works
   - Edit any Java file and save — it should auto-format via Spotless
   - Edit any `.tsx` file and save — it should auto-format via Prettier
   - Try committing without formatting: the pre-commit hook should reject it

## Troubleshooting

**Devcontainer fails to build**
Check that Docker Desktop is running and has enough resources (4 GB+ RAM recommended).

**Infrastructure containers won't start**
```
docker compose down
docker compose up -d
```
If a port is already in use, stop whatever is using it or change the port in `docker-compose.yml`.

**Backend can't connect to Postgres**
The backend connects to `localhost:5432` in the `dev` profile. Confirm Postgres is up:
```
docker compose ps
```

**Pre-commit hook not running**
```
npm run prepare
```

**Formatting not working on save**
Check the VS Code status bar (bottom right) for the active formatter. The extensions
from `devcontainer.json` are installed automatically on first open — if one is missing,
run "Developer: Reload Window" from the command palette.
