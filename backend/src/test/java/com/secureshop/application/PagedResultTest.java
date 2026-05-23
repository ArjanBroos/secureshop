package com.secureshop.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PagedResultTest {

    @Test
    void rejectsNullItems() {
        assertThatThrownBy(() -> new PagedResult<>(null, 0, 0, 10))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNegativeTotalItems() {
        assertThatThrownBy(() -> new PagedResult<>(List.of(), -1, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalItems");
    }

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> new PagedResult<>(List.of(), 0, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void rejectsZeroSize() {
        assertThatThrownBy(() -> new PagedResult<>(List.of(), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }
}
