# ADR-005: Domain Modeling Principles

## Status: Accepted

## Context

Domain classes can be written in two ways. The anemic style treats them as plain
data containers — fields, getters, no logic — and puts all behaviour in service
classes. The rich style puts logic where it belongs: on the objects that own the
data.

The anemic style is tempting because it is simple to start with, but it leads to
services that are full of procedural logic, business rules scattered across the
codebase, and no single place to look for what a concept actually means.

Related to this is how identifiers and value concepts are typed. Using raw
primitives (`UUID`, `BigDecimal`, `String`) for everything makes it possible to
pass a `ProductId` where an `OrderId` is expected, or to construct a `Product`
with a negative price, and the compiler will not complain.

## Decision

The following rules apply to all domain code in `com.secureshop.domain`:

**1. Strongly typed IDs**
Every entity has its own ID record (e.g. `ProductId`, `OrderId`) that wraps a
`UUID`. Passing the wrong ID type is then a compile error, not a runtime bug.

**2. Value objects for anything with an invariant**
If a concept has a rule — price cannot be negative, name cannot be blank — it
gets its own record that enforces that rule in its compact constructor. A
`Money` record that rejects negative amounts is safer than a `BigDecimal` with
a comment saying "must be positive".

**3. Invalid state must not be representable**
All invariants are enforced in the constructor. An object that has been
constructed is always valid. There is no separate `validate()` method to
remember to call.

**4. No anemic domain model**
Logic that belongs to a concept lives on that concept. A `Cart` knows how to
calculate its total. A `Money` knows how to add itself to another `Money`.
Services orchestrate; they do not contain business logic.

**5. Javadoc on all public classes, interfaces, and methods**
Public API is documented with intent, not just a restatement of the name.

## Consequences

- More types to write upfront, but each type is small and self-contained.
- Business rules are co-located with the data they govern — easy to find, easy
  to test.
- The compiler enforces invariants, so unit tests focus on behaviour rather than
  input validation.
- New contributors need to understand the value object pattern. The payoff is
  that the domain package reads like a description of the business.
