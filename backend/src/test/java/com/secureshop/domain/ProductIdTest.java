package com.secureshop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductIdTest {

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new ProductId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void generateProducesNonNullId() {
        assertThat(ProductId.generate().value()).isNotNull();
    }

    @Test
    void generateProducesUniqueIds() {
        assertThat(ProductId.generate()).isNotEqualTo(ProductId.generate());
    }

    @Test
    void ofWrapsExistingUuid() {
        var uuid = UUID.randomUUID();
        assertThat(ProductId.of(uuid).value()).isEqualTo(uuid);
    }
}
