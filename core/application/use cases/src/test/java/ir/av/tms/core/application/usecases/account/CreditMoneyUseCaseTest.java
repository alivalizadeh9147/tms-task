package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.CreditMoneyRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.event.DepositedMoneyEvent;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditMoneyUseCaseTest {

    @Mock
    private AccountRepository repository;

    private CreditMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreditMoneyUseCase(repository);
    }

    @Test
    void shouldCreditMoneyToAccount() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result =
                useCase.execute(request);

        verify(repository).findForUpdate(
                new AccountId(accountId)
        );

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("100"))
                        )
                )
        );

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.empty());

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldThrowWhenAmountIsZero() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldNotUpdateRepositoryWhenDomainOperationFails() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldReturnDomainEvents() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        accountId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result =
                useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.events());

        assertEquals(1, result.events().size());

        assertInstanceOf(
                DepositedMoneyEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldUpdateRepositoryWithCreditedAccount() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        accountId,
                        new BigDecimal("250.75")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.getId().equals(account.getId())
                                && updated.balance().equals(
                                Money.of(new BigDecimal("250.75"))
                        )
                )
        );
    }

    @Test
    void shouldFindAccountForUpdateBeforeUpdatingBalance() {
        UUID accountId = UUID.randomUUID();

        Account account = Account.open("Ali");

        when(repository.findForUpdate(new AccountId(accountId)))
                .thenReturn(Optional.of(account));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
}