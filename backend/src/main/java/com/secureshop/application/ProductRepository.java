package com.secureshop.application;

import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import java.util.Optional;

/**
 * Port for retrieving products from persistent storage.
 *
 * <p>This interface is defined by the application layer and implemented by the infrastructure
 * layer. Nothing here implies a specific database or framework.
 */
public interface ProductRepository {

    /**
     * Returns a page of all products.
     *
     * @param page zero-based page index
     * @param size maximum number of items per page
     */
    PagedResult<Product> findAll(int page, int size);

    /** Returns the product with the given ID, or empty if not found. */
    Optional<Product> findById(ProductId id);
}
