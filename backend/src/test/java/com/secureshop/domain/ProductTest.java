package com.secureshop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductTest {

    private static final ProductId ID = ProductId.generate();
    private static final Money PRICE = Money.euros(999);

    @Test
    void rejectsNullId() {
        assertThatThrownBy(() -> new Product(null, "Chair", "A chair", PRICE, "http://img"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> new Product(ID, null, "A chair", PRICE, "http://img"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new Product(ID, "  ", "A chair", PRICE, "http://img"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsNullDescription() {
        assertThatThrownBy(() -> new Product(ID, "Chair", null, PRICE, "http://img"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullPrice() {
        assertThatThrownBy(() -> new Product(ID, "Chair", "A chair", null, "http://img"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullImageUrl() {
        assertThatThrownBy(() -> new Product(ID, "Chair", "A chair", PRICE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imageUrl");
    }

    @Test
    void rejectsBlankImageUrl() {
        assertThatThrownBy(() -> new Product(ID, "Chair", "A chair", PRICE, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imageUrl");
    }

    @Test
    void constructsValidProduct() {
        var product = new Product(ID, "Chair", "A comfortable chair", PRICE, "http://img");
        assertThat(product.id()).isEqualTo(ID);
        assertThat(product.name()).isEqualTo("Chair");
        assertThat(product.description()).isEqualTo("A comfortable chair");
        assertThat(product.price()).isEqualTo(PRICE);
        assertThat(product.imageUrl()).isEqualTo("http://img");
    }
}
