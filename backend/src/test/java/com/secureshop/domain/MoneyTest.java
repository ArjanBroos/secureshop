package com.secureshop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Currency;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void rejectsNullCurrency() {
        assertThatThrownBy(() -> new Money(100, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Money(-1, Currency.getInstance("EUR")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void allowsZeroAmount() {
        var money = new Money(0, Currency.getInstance("EUR"));
        assertThat(money.amountInCents()).isEqualTo(0);
    }

    @Test
    void eurosFactoryCreatesEuroMoney() {
        var money = Money.euros(999);
        assertThat(money.amountInCents()).isEqualTo(999);
        assertThat(money.currency()).isEqualTo(Currency.getInstance("EUR"));
    }
}
