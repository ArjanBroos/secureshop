package com.secureshop.domain;

import java.util.Currency;
import java.util.Objects;

/**
 * An amount of money in a specific currency, stored in the smallest currency unit.
 *
 * <p>For example, €9.99 is represented as {@code new Money(999, Currency.getInstance("EUR"))}.
 * Storing in cents avoids floating-point rounding issues and makes fractional cents impossible to
 * represent.
 *
 * <p>Invariants: amount is non-negative; currency is non-null.
 */
public record Money(long amountInCents, Currency currency) {

    public Money {
        Objects.requireNonNull(currency, "Currency must not be null");
        if (amountInCents < 0) throw new IllegalArgumentException("Amount must not be negative");
    }

    /** Convenience factory for euro amounts expressed in cents. */
    public static Money euros(long amountInCents) {
        return new Money(amountInCents, Currency.getInstance("EUR"));
    }
}
