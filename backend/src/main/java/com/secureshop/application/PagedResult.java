package com.secureshop.application;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A single page of results from a paginated query.
 *
 * @param <T> the type of items in this page
 * @param items the items on this page
 * @param totalItems the total number of items across all pages
 * @param page the zero-based page index
 * @param size the maximum number of items per page
 */
public record PagedResult<T>(List<T> items, long totalItems, int page, int size) {

    public PagedResult {
        Objects.requireNonNull(items, "items must not be null");
        if (totalItems < 0) throw new IllegalArgumentException("totalItems must not be negative");
        if (page < 0) throw new IllegalArgumentException("page must not be negative");
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
    }

    /** Returns a new {@code PagedResult} with each item transformed by {@code mapper}. */
    public <R> PagedResult<R> map(Function<? super T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new PagedResult<R>(items.stream().map(mapper).toList(), totalItems, page, size);
    }
}
