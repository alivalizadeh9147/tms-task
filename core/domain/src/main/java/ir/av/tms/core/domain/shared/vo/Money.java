package ir.av.tms.core.domain.shared.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount) {

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Money amount must not be negative"
            );
        }

        amount = normalize(amount);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        return new Money(
                this.amount.add(other.amount)
        );
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        if (this.isLessThan(other)) {
            throw new IllegalArgumentException(
                    "Money cannot become negative"
            );
        }

        return new Money(
                this.amount.subtract(other.amount)
        );
    }

    public boolean isGreaterThan(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqualTo(Money other) {
        Objects.requireNonNull(other, "Money must not be null");

        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    private static BigDecimal normalize(BigDecimal amount) {
        if (amount.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return amount.stripTrailingZeros();
    }
}