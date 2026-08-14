package ir.av.tms.core.domain.account.entity;

import ir.av.tms.core.domain.account.event.AccountOpenedEvent;
import ir.av.tms.core.domain.account.event.DebitMoneyEvent;
import ir.av.tms.core.domain.account.event.DepositedMoneyEvent;
import ir.av.tms.core.domain.account.exception.InvalidAccountOperationException;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.entity.AggregateRoot;
import ir.av.tms.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.tms.core.domain.shared.vo.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Account extends AggregateRoot<AccountId> {

    private final Money balance;
    private final String name;

    private Account(
            AccountId accountId,
            Money balance,
            String name
    ) {
        super(accountId);

        this.balance = Objects.requireNonNull(
                balance,
                "Balance must not be null"
        );
        this.name = requireValidName(name);
    }

    private static String requireValidName(String name) {
        Objects.requireNonNull(name, "Name must not be null");

        if (name.isBlank()) {
            throw new InvalidAccountOperationException(
                    "Name must not be blank"
            );
        }

        return name;
    }

    public static Account open(
            String name
    ) {
        Account account = new Account(
                AccountId.open(),
                Money.zero(),
                name
        );
        AccountOpenedEvent.Payload payload = new AccountOpenedEvent.Payload();
        AccountOpenedEvent accountOpenedEvent = new AccountOpenedEvent(account.getId().value(),
                UUID.randomUUID(), Instant.now(), payload);
        account.addEvent(accountOpenedEvent);
        return account;
    }

    public Account deposit(Money amount) {
        requirePositiveAmount(amount);

        Money add = balance.add(amount);

        Account build = copyBuilder()
                .balance(add)
                .build();
        DepositedMoneyEvent.Payload payload = new DepositedMoneyEvent.Payload();
        DepositedMoneyEvent depositedMoneyEvent = new DepositedMoneyEvent(build.getId().value(),
                UUID.randomUUID(), Instant.now(), payload);
        build.addEvent(depositedMoneyEvent);
        return build;
    }

    public Account debit(Money amount) {
        requirePositiveAmount(amount);

        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(
                    getId()
            );
        }

        Account build = copyBuilder()
                .balance(balance.subtract(amount))
                .build();
        DebitMoneyEvent.Payload payload = new DebitMoneyEvent.Payload();
        DebitMoneyEvent debitMoneyEvent = new DebitMoneyEvent(build.getId().value(),
                UUID.randomUUID(), Instant.now(), payload);
        build.addEvent(debitMoneyEvent);
        return build;
    }

    public Money balance() {
        return balance;
    }

    public String name() {
        return name;
    }

    private static void requirePositiveAmount(Money amount) {
        Objects.requireNonNull(
                amount,
                "Amount must not be null"
        );

        if (amount.isZero()) {
            throw new InvalidAccountOperationException(
                    "Amount must be greater than zero"
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private AccountId id;
        private Money balance;
        private String name;

        private Builder() {
        }

        public Builder id(AccountId accountId) {
            this.id = accountId;
            return this;
        }

        public Builder balance(Money balance) {
            this.balance = balance;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Account build() {
            return new Account(
                    id,
                    balance,
                    name
            );
        }
    }

    private Builder copyBuilder() {
        return new Builder()
                .id(getId())
                .balance(balance)
                .name(name)
                ;
    }

    @Override
    public String toString() {
        return "Account{" +
                "balance=" + balance +
                ", name='" + name + '\'' +
                '}';
    }
}