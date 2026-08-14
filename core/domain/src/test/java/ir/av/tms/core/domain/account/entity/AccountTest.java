package ir.av.tms.core.domain.account.entity;

import ir.av.tms.core.domain.account.event.AccountOpenedEvent;
import ir.av.tms.core.domain.account.event.DebitMoneyEvent;
import ir.av.tms.core.domain.account.event.DepositedMoneyEvent;
import ir.av.tms.core.domain.account.exception.InvalidAccountOperationException;
import ir.av.tms.core.domain.shared.event.DomainEvent;
import ir.av.tms.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.tms.core.domain.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldOpenAccountWithZeroBalance() {

        Account account = Account.open("Ali");

        assertNotNull(account.getId());
        assertEquals("Ali", account.name());
        assertEquals(Money.zero(), account.balance());
    }

    @Test
    void shouldGenerateAccountOpenedEventWhenAccountIsOpened() {

        Account account = Account.open("Ali");

        List<DomainEvent<?>> events =
                account.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                AccountOpenedEvent.class,
                events.getFirst()
        );
    }

    @Test
    void shouldUseAccountIdInAccountOpenedEvent() {

        Account account = Account.open("Ali");

        AccountOpenedEvent event =
                (AccountOpenedEvent) account.pullDomainEvents().getFirst();

        assertEquals(
                account.getId().value(),
                event.aggregateId()
        );
    }

    @Test
    void shouldDepositMoney() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertEquals(
                Money.of(new BigDecimal("0")),
                account.balance()
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );
    }

    @Test
    void shouldGenerateDepositedMoneyEvent() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        List<DomainEvent<?>> events =
                deposited.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                DepositedMoneyEvent.class,
                events.getFirst()
        );
    }

    @Test
    void shouldNotAllowZeroDeposit() {

        Account account = Account.open("Ali");

        assertThrows(
                InvalidAccountOperationException.class,
                () -> account.deposit(Money.zero())
        );
    }

    @Test
    void shouldNotAllowNullDeposit() {

        Account account = Account.open("Ali");

        assertThrows(
                NullPointerException.class,
                () -> account.deposit(null)
        );
    }

    @Test
    void shouldDebitMoney() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        Account debited = deposited.debit(
                Money.of(new BigDecimal("40"))
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );

        assertEquals(
                Money.of(new BigDecimal("60")),
                debited.balance()
        );
    }

    @Test
    void shouldNotDebitMoreThanBalance() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> deposited.debit(
                        Money.of(new BigDecimal("101"))
                )
        );
    }

    @Test
    void shouldKeepBalanceUnchangedWhenDebitFails() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> deposited.debit(
                        Money.of(new BigDecimal("101"))
                )
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );
    }

    @Test
    void shouldDebitEntireBalance() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        Account debited = deposited.debit(
                Money.of(new BigDecimal("100"))
        );

        assertEquals(
                Money.zero(),
                debited.balance()
        );
    }

    @Test
    void shouldNotAllowZeroDebit() {

        Account account = Account.open("Ali");

        assertThrows(
                InvalidAccountOperationException.class,
                () -> account.debit(Money.zero())
        );
    }

    @Test
    void shouldGenerateDebitMoneyEvent() {

        Account account = Account.open("Ali");

        Account deposited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        Account debited = deposited.debit(
                Money.of(new BigDecimal("40"))
        );

        List<DomainEvent<?>> events =
                debited.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                DebitMoneyEvent.class,
                events.getFirst()
        );
    }
}
