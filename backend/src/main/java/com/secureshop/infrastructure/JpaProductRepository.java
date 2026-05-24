package com.secureshop.infrastructure;

import com.secureshop.application.PagedResult;
import com.secureshop.application.ProductRepository;
import com.secureshop.domain.Product;
import com.secureshop.domain.ProductId;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of {@link ProductRepository}.
 *
 * <p>Extends {@link SimpleJpaRepository} to reuse Spring Data's built-in paging and lookup support,
 * while implementing the application port with domain types.
 */
@Repository
@Transactional(readOnly = true)
class JpaProductRepository extends SimpleJpaRepository<ProductJpaEntity, UUID>
        implements ProductRepository {

    JpaProductRepository(EntityManager em) {
        super(ProductJpaEntity.class, em);
    }

    @Override
    public PagedResult<Product> findAll(int page, int size) {
        Page<ProductJpaEntity> jpaPage = findAll(PageRequest.of(page, size));
        List<Product> items =
                jpaPage.getContent().stream().map(ProductJpaEntity::toDomain).toList();
        return new PagedResult<>(items, jpaPage.getTotalElements(), page, size);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return findById(id.value()).map(ProductJpaEntity::toDomain);
    }
}
