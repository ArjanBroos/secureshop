package com.secureshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.secureshop.application.PagedResult;
import com.secureshop.application.ViewCatalogUseCase;
import com.secureshop.domain.Money;
import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ViewCatalogUseCase viewCatalog;

    @Test
    void getProducts_returns_paged_list() throws Exception {
        ProductId id = ProductId.generate();
        Product widget =
                new Product(
                        id,
                        "Widget",
                        "A useful widget",
                        Money.euros(999),
                        "https://example.com/widget.png");
        when(viewCatalog.getProducts(0, 20))
                .thenReturn(new PagedResult<>(List.of(widget), 1, 0, 20));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Widget"))
                .andExpect(jsonPath("$.items[0].priceCents").value(999))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void getProductById_returns_product_when_found() throws Exception {
        ProductId id = ProductId.generate();
        Product widget =
                new Product(
                        id,
                        "Widget",
                        "A useful widget",
                        Money.euros(999),
                        "https://example.com/widget.png");
        when(viewCatalog.getProductById(id)).thenReturn(Optional.of(widget));

        mockMvc.perform(get("/products/{id}", id.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.value().toString()))
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.priceCents").value(999));
    }

    @Test
    void getProductById_returns_404_when_not_found() throws Exception {
        when(viewCatalog.getProductById(any(ProductId.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/{id}", ProductId.generate().value()))
                .andExpect(status().isNotFound());
    }
}
