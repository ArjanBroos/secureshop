package com.secureshop.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.secureshop.TestcontainersConfiguration;
import com.secureshop.application.PagedResult;
import com.secureshop.application.ProductRepository;
import com.secureshop.domain.Money;
import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class JpaProductRepositoryTest {

    @Autowired ProductRepository repository;
    @PersistenceContext EntityManager em;

    @Test
    void findAll_returns_saved_products_paged() {
        em.persist(
                ProductJpaEntity.from(
                        new Product(
                                ProductId.generate(),
                                "Widget",
                                "A useful widget",
                                Money.euros(999),
                                "https://example.com/widget.png")));
        em.flush();

        PagedResult<Product> result = repository.findAll(0, 10);

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.items().get(0).name()).isEqualTo("Widget");
    }

    @Test
    void findById_returns_product_when_found() {
        ProductId id = ProductId.generate();
        em.persist(
                ProductJpaEntity.from(
                        new Product(
                                id,
                                "Gadget",
                                "A fancy gadget",
                                Money.euros(1499),
                                "https://example.com/gadget.png")));
        em.flush();

        Optional<Product> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(id);
        assertThat(result.get().price()).isEqualTo(Money.euros(1499));
    }

    @Test
    void findById_returns_empty_when_not_found() {
        assertThat(repository.findById(ProductId.generate())).isEmpty();
    }
}
