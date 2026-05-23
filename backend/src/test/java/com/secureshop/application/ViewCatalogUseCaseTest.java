package com.secureshop.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureshop.domain.Money;
import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ViewCatalogUseCaseTest {

    private ProductRepository repository;
    private ViewCatalogUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        useCase = new ViewCatalogUseCase(repository);
    }

    @Test
    void rejectsNullRepository() {
        assertThatThrownBy(() -> new ViewCatalogUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getProductsDelegatesToRepository() {
        var expected = new PagedResult<Product>(List.of(), 0, 0, 10);
        when(repository.findAll(0, 10)).thenReturn(expected);

        var result = useCase.getProducts(0, 10);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll(0, 10);
    }

    @Test
    void getProductByIdDelegatesToRepository() {
        var id = ProductId.generate();
        var product =
                new Product(id, "Chair", "A comfortable chair", Money.euros(999), "http://img");
        when(repository.findById(id)).thenReturn(Optional.of(product));

        var result = useCase.getProductById(id);

        assertThat(result).contains(product);
        verify(repository).findById(id);
    }

    @Test
    void getProductByIdRejectsNullId() {
        assertThatThrownBy(() -> useCase.getProductById(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getProductByIdReturnsEmptyWhenNotFound() {
        var id = ProductId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        var result = useCase.getProductById(id);

        assertThat(result).isEmpty();
    }
}
