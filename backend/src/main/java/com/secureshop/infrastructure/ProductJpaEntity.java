package com.secureshop.infrastructure;

import com.secureshop.domain.Money;
import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Currency;
import java.util.UUID;

/**
 * JPA entity that maps the {@code products} table to the database.
 *
 * <p>This class is an infrastructure detail — it must not leak into the domain or application
 * layers. Use {@link #from(Product)} to persist a domain object and {@link #toDomain()} to
 * reconstruct one.
 */
@Entity
@Table(name = "products")
class ProductJpaEntity {

    @Id private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /** Required by JPA — not for direct use. */
    protected ProductJpaEntity() {}

    private ProductJpaEntity(
            UUID id,
            String name,
            String description,
            long priceCents,
            String currency,
            String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.currency = currency;
        this.imageUrl = imageUrl;
    }

    /** Creates a new entity from a domain {@link Product}. */
    static ProductJpaEntity from(Product product) {
        return new ProductJpaEntity(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amountInCents(),
                product.price().currency().getCurrencyCode(),
                product.imageUrl());
    }

    /** Converts this entity back to a domain {@link Product}. */
    Product toDomain() {
        return new Product(
                ProductId.of(id),
                name,
                description,
                new Money(priceCents, Currency.getInstance(currency)),
                imageUrl);
    }
}
