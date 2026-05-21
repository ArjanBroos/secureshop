# Deploy to Production

## Normal flow (automated)

1. Open a pull request against `main`
2. GitHub runs `checks.yml` — formatting, tests, Docker build, Trivy scan
3. Get the PR reviewed and approved
4. Merge the PR
5. `deploy.yml` triggers automatically:
   - Builds Docker images tagged with the commit SHA
   - Pushes images to GHCR
   - Deploys to staging and waits for the health check to pass
6. Go to **GitHub → Actions → Deploy → the running workflow**
7. The `deploy-production` job shows **"Waiting for approval"**
8. Click **Review deployments**, select `production`, and approve
9. Production deploys the same image SHA that was deployed to staging
10. The workflow polls `/actuator/health` until it returns `UP`

Verify: open the production URL, check the health endpoint, confirm the
frontend shows "Backend: UP".

---

## Emergency rollback

Render keeps a deploy history for each service.

1. Go to **Render dashboard → the affected service → Deploys**
2. Find the last known-good deploy
3. Click **Redeploy**
4. Wait for health checks to pass
5. Investigate the issue on a branch — never debug directly in production

---

## Manual deploy (if GitHub Actions is unavailable)

1. Build and push images locally:

   ```bash
   docker build -t ghcr.io/<owner>/secureshop-backend:<sha> ./backend
   docker build -t ghcr.io/<owner>/secureshop-frontend:<sha> ./frontend
   echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin
   docker push ghcr.io/<owner>/secureshop-backend:<sha>
   docker push ghcr.io/<owner>/secureshop-frontend:<sha>
   ```

2. Trigger the deploy hooks manually:

   ```bash
   curl -sf -X POST "<RENDER_PROD_BACKEND_DEPLOY_HOOK>"
   curl -sf -X POST "<RENDER_PROD_FRONTEND_DEPLOY_HOOK>"
   ```

3. This is an emergency procedure — open an issue to restore CI immediately.
