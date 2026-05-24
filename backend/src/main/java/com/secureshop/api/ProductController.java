package com.secureshop.api;

import com.secureshop.application.PagedResult;
import com.secureshop.application.ViewCatalogUseCase;
import com.secureshop.domain.ProductId;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for browsing the product catalogue. */
@RestController
@RequestMapping("/products")
class ProductController {

    private final ViewCatalogUseCase viewCatalog;

    ProductController(ViewCatalogUseCase viewCatalog) {
        this.viewCatalog = viewCatalog;
    }

    @GetMapping
    PagedResult<ProductResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return viewCatalog.getProducts(page, size).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return viewCatalog
                .getProductById(ProductId.of(id))
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
