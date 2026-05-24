package com.secureshop.api;

import com.secureshop.domain.Product;
import java.util.UUID;

/**
 * API response representation of a {@link Product}.
 *
 * <p>Translates domain types to JSON-serializable primitives, keeping the API contract independent
 * of domain model evolution.
 */
record ProductResponse(
        UUID id,
        String name,
        String description,
        long priceCents,
        String currency,
        String imageUrl) {

    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amountInCents(),
                product.price().currency().getCurrencyCode(),
                product.imageUrl());
    }
}
