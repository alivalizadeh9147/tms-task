package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.DebitMoneyRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.event.DebitMoneyEvent;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.tms.core.domain.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebitMoneyUseCaseTest {

    @Mock
    private AccountRepository repository;

    private DebitMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DebitMoneyUseCase(repository);
    }

    @Test
    void shouldDebitMoneyFromAccount() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");
        Account credited = account.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result = useCase.execute(request);

        verify(repository).findForUpdate(
                new AccountId(accountId)
        );

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("400"))
                        )
                )
        );

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.empty());

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        assertThrows(
                AccountNotFoundException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdate(
                new AccountId(accountId)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("101")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdate(
                new AccountId(accountId)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldDebitEntireBalance() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(Money.zero())
                )
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(account));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        BigDecimal.ZERO
                );

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(account));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("-100")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldReturnDebitDomainEvent() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result = useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.events());
        assertEquals(1, result.events().size());

        assertInstanceOf(
                DebitMoneyEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldUpdateRepositoryWithWithdrawnAccount() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("150")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("350"))
                        )
                )
        );
    }

    @Test
    void shouldFindAccountForUpdateBeforeUpdatingBalance() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        useCase.execute(request);

        var inOrder = inOrder(repository);

        inOrder.verify(repository)
                .findForUpdate(new AccountId(accountId));

        inOrder.verify(repository)
                .updateBalance(any(Account.class));
    }

    @Test
    void shouldNotUpdateRepositoryWhenDebitFails() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        Account credited = account.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        accountId,
                        new BigDecimal("200")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }
}