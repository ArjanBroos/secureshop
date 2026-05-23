# ADR-004: Use Trivy for Vulnerability Scanning Instead of OWASP Dependency-Check

## Status: Accepted

## Context

The plan called for running `./mvnw org.owasp:dependency-check-maven:check` in CI
to catch CVEs in Java dependencies. Trivy is already in the CI pipeline scanning
both Docker images after they are built.

Both tools scan dependencies against the NVD, so there is significant overlap for
Java projects: Trivy understands `pom.xml` and catches the same Maven CVEs that
OWASP Dependency-Check would find.

OWASP Dependency-Check has meaningful operational costs:
- The NVD now rate-limits its data feed. Without a paid API key, downloads are
  slow and unreliable in CI.
- The default CVSS failure threshold (11) never triggers — effective use requires
  additional configuration.
- It only covers Maven dependencies. npm vulnerabilities require a separate tool
  regardless.

Trivy scans the full Docker image — OS packages, installed binaries, and
application dependencies — giving broader coverage than OWASP Dependency-Check
for less setup. It also covers the frontend image's npm dependencies in the same
step.

## Decision

Rely on Trivy for vulnerability scanning. Do not add OWASP Dependency-Check to
the pipeline.

## Consequences

- CVE feedback arrives slightly later in the pipeline (after image build) rather
  than in the backend job. This is an acceptable trade-off.
- A vulnerable OS package in the base image is caught by Trivy but would have
  been invisible to OWASP Dependency-Check entirely.
- No NVD API key is needed.
- Both the backend and frontend images are covered by a single tool.
