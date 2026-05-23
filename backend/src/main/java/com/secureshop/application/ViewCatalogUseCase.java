package com.secureshop.application;

import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import java.util.Objects;
import java.util.Optional;

/**
 * Use case for browsing the product catalogue.
 *
 * <p>Read-only: no state is modified by any method here.
 */
public class ViewCatalogUseCase {

    private final ProductRepository productRepository;

    /** Creates a new {@code ViewCatalogUseCase} backed by the given repository. */
    public ViewCatalogUseCase(ProductRepository productRepository) {
        this.productRepository =
                Objects.requireNonNull(productRepository, "productRepository must not be null");
    }

    /**
     * Returns a page of products from the catalogue.
     *
     * @param page zero-based page index
     * @param size maximum number of items per page
     */
    public PagedResult<Product> getProducts(int page, int size) {
        return productRepository.findAll(page, size);
    }

    /** Returns the product with the given ID, or empty if it does not exist. */
    public Optional<Product> getProductById(ProductId id) {
        Objects.requireNonNull(id, "id must not be null");
        return productRepository.findById(id);
    }
}
