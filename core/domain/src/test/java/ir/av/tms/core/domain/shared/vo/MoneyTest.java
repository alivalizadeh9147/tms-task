package ir.av.tms.core.domain.shared.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoney() {
        Money money = Money.of(new BigDecimal("100.50"));

        assertEquals(
                new BigDecimal("100.5"),
                money.amount()
        );
    }

    @Test
    void shouldNormalizeTrailingZeros() {
        Money first = Money.of(new BigDecimal("10.125"));
        Money second = Money.of(new BigDecimal("10.12500"));

        assertEquals(first.amount(), second.amount());
        assertEquals(first, second);
    }

    @Test
    void shouldNormalizeZero() {
        Money money = Money.of(new BigDecimal("0.0000"));

        assertEquals(BigDecimal.ZERO, money.amount());
        assertTrue(money.isZero());
    }

    @Test
    void shouldCreateZeroMoney() {
        Money money = Money.zero();

        assertEquals(BigDecimal.ZERO, money.amount());
        assertTrue(money.isZero());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                NullPointerException.class,
                () -> Money.of(null)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1"))
        );
    }

    @Test
    void shouldAddTwoMoneyValues() {
        Money first = Money.of(new BigDecimal("10.125"));
        Money second = Money.of(new BigDecimal("5.12500"));

        Money result = first.add(second);

        assertEquals(
                new BigDecimal("15.25"),
                result.amount()
        );
    }

    @Test
    void shouldSubtractTwoMoneyValues() {
        Money first = Money.of(new BigDecimal("20"));
        Money second = Money.of(new BigDecimal("7.50"));

        Money result = first.subtract(second);

        assertEquals(
                new BigDecimal("12.5"),
                result.amount()
        );
    }

    @Test
    void shouldNotAllowMoneyToBecomeNegative() {
        Money first = Money.of(new BigDecimal("10"));
        Money second = Money.of(new BigDecimal("20"));

        assertThrows(
                IllegalArgumentException.class,
                () -> first.subtract(second)
        );
    }

    @Test
    void shouldReturnTrueWhenGreaterThan() {
        Money first = Money.of(new BigDecimal("20"));
        Money second = Money.of(new BigDecimal("10"));

        assertTrue(first.isGreaterThan(second));
    }

    @Test
    void shouldReturnFalseWhenNotGreaterThan() {
        Money first = Money.of(new BigDecimal("10"));
        Money second = Money.of(new BigDecimal("20"));

        assertFalse(first.isGreaterThan(second));
    }

    @Test
    void shouldReturnTrueWhenGreaterThanOrEqual() {
        Money first = Money.of(new BigDecimal("10.125"));
        Money second = Money.of(new BigDecimal("10.12500"));

        assertTrue(first.isGreaterThanOrEqualTo(second));
    }

    @Test
    void shouldReturnTrueWhenLessThan() {
        Money first = Money.of(new BigDecimal("10"));
        Money second = Money.of(new BigDecimal("20"));

        assertTrue(first.isLessThan(second));
    }

    @Test
    void shouldReturnTrueWhenLessThanOrEqual() {
        Money first = Money.of(new BigDecimal("10"));
        Money second = Money.of(new BigDecimal("10.00"));

        assertTrue(first.isLessThanOrEqualTo(second));
    }

    @Test
    void shouldReturnTrueWhenMoneyIsZero() {
        Money money = Money.zero();

        assertTrue(money.isZero());
    }

    @Test
    void shouldRejectNullMoneyInAdd() {
        Money money = Money.of(new BigDecimal("10"));

        assertThrows(
                NullPointerException.class,
                () -> money.add(null)
        );
    }

    @Test
    void shouldRejectNullMoneyInSubtract() {
        Money money = Money.of(new BigDecimal("10"));

        assertThrows(
                NullPointerException.class,
                () -> money.subtract(null)
        );
    }
}