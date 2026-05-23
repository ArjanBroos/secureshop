package com.secureshop.domain;

import java.util.Objects;

/**
 * A product available in the shop catalogue.
 *
 * <p>Invariants: all fields are non-null; name and imageUrl are non-blank.
 */
public record Product(ProductId id, String name, String description, Money price, String imageUrl) {

    public Product {
        Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be blank");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(price, "price must not be null");
        if (imageUrl == null || imageUrl.isBlank())
            throw new IllegalArgumentException("imageUrl must not be blank");
    }
}
