package com.secureshop.config;

import com.secureshop.application.ProductRepository;
import com.secureshop.application.ViewCatalogUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires application-layer use cases with their dependencies.
 *
 * <p>Keeps the application layer free of Spring annotations while still registering use cases as
 * Spring beans.
 */
@Configuration
class ApplicationConfiguration {

    @Bean
    ViewCatalogUseCase viewCatalogUseCase(ProductRepository productRepository) {
        return new ViewCatalogUseCase(productRepository);
    }
}
