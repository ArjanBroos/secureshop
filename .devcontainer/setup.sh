#!/bin/bash
set -euo pipefail

# Fix volume ownership (Docker creates named volumes as root)
sudo chown -R vscode:vscode /home/vscode/.npm /home/vscode/.m2

# Install gitleaks (secret scanner for pre-commit hook)
GITLEAKS_VERSION="8.30.1"
sudo sh -c "curl -sSfL https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz | tar xz -C /usr/local/bin gitleaks"

# Install root tooling (husky, lint-staged, prettier) and initialise git hooks
npm ci

# Install frontend dependencies
cd /workspace/frontend && npm ci

# Pre-download Maven dependencies so first backend build is fast
cd /workspace/backend && ./mvnw dependency:resolve
