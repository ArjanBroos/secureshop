# ADR-001: Use Valkey Instead of Redis

## Status: Accepted

## Context

We need an in-memory data store for session storage and caching. Redis is the
industry standard, but since Redis 8.0 the project uses a tri-license model
(RSALv2 / SSPLv1 / AGPLv3) rather than the original BSD license:

- **RSALv2** prohibits use in services that compete with Redis
- **SSPLv1** requires open-sourcing the entire delivery stack if the software
  is offered as a SaaS — highly restrictive for commercial products
- **AGPLv3** requires publishing application source when the app is served over
  a network, making it incompatible with closed-source commercial software

Even for non-commercial projects, many organisations block all three licenses
by policy. Establishing the right habit now avoids a painful swap later.

## Decision

Use **Valkey** instead of Redis. Valkey is a BSD-licensed fork of Redis 7.2
created in response to the license change. It is maintained by the Linux
Foundation with backing from AWS, Google Cloud, and other major vendors.

Valkey is wire-compatible with Redis: the same client libraries, the same
commands, and the same configuration all work without modification.

## Consequences

- No licensing risk for commercial or SaaS use
- Existing Redis client libraries (Jedis, Lettuce, Spring Data Redis) work
  unchanged — the wire protocol is identical
- Valkey is actively maintained and tracking upstream Redis features
- Less name recognition than Redis, but functionally identical for this use case
