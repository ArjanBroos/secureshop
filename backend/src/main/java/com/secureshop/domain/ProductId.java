package com.secureshop.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for a {@link Product}.
 *
 * <p>Using a dedicated type instead of a raw {@link UUID} prevents accidentally passing a product
 * ID where an order ID (or any other ID) is expected.
 */
public record ProductId(UUID value) {

    public ProductId {
        Objects.requireNonNull(value, "ProductId value must not be null");
    }

    /** Creates a new random {@code ProductId}. */
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    /** Wraps an existing {@link UUID} as a {@code ProductId}. */
    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}
