package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.TransferMoneyRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.exception.InvalidAccountOperationException;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.tms.core.domain.shared.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferMoneyUseCaseTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private TransferMoneyUseCase useCase;

    @Test
    void should_transfer_money_successfully() {
        Account source = Account.open("Source")
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Account destination = Account.open("Destination");

        UUID sourceId = source.getId().value();
        UUID destinationId = destination.getId().value();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        UseCaseResult<Void> result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.events()).hasSize(2);

        ArgumentCaptor<Account> accountCaptor =
                ArgumentCaptor.forClass(Account.class);

        verify(repository, times(2))
                .updateBalance(accountCaptor.capture());

        List<Account> updatedAccounts = accountCaptor.getAllValues();

        Account updatedSource = updatedAccounts.stream()
                .filter(account -> account.getId().value().equals(sourceId))
                .findFirst()
                .orElseThrow();

        Account updatedDestination = updatedAccounts.stream()
                .filter(account -> account.getId().value().equals(destinationId))
                .findFirst()
                .orElseThrow();

        assertThat(updatedSource.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(400)));

        assertThat(updatedDestination.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(100)));
    }

    @Test
    void should_reject_transfer_between_same_account() {
        Account account = Account.open("Account");

        UUID accountId = account.getId().value();

        TransferMoneyRequest request = new TransferMoneyRequest(
                accountId,
                accountId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidAccountOperationException.class)
                .hasMessage("Source and destination accounts must be different");

        verifyNoInteractions(repository);
    }

    @Test
    void should_throw_when_source_account_not_found() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of());

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Source account");

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_throw_when_destination_account_not_found() {
        Account source = Account.open("Source");

        UUID sourceId = source.getId().value();
        UUID destinationId = UUID.randomUUID();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Destination account");

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_throw_when_source_has_insufficient_balance() {
        Account source = Account.open("Source");
        Account destination = Account.open("Destination");

        UUID sourceId = source.getId().value();
        UUID destinationId = destination.getId().value();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_lock_accounts_in_deterministic_order() {
        Account source = Account.open("Source");
        Account destination = Account.open("Destination");

        UUID sourceId = source.getId().value();
        UUID destinationId = destination.getId().value();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        ArgumentCaptor<List<AccountId>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(repository).findAllForUpdate(captor.capture());

        List<AccountId> lockedIds = captor.getValue();

        assertThat(lockedIds)
                .containsExactly(
                        lockedIds.get(0),
                        lockedIds.get(1)
                );

        assertThat(lockedIds.get(0).value())
                .isLessThan(lockedIds.get(1).value());
    }

    @Test
    void should_update_both_accounts_exactly_once() {
        Account source = Account.open("Source")
                .deposit(Money.of(BigDecimal.valueOf(1000)));

        Account destination = Account.open("Destination");

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.getId().value(),
                destination.getId().value(),
                BigDecimal.valueOf(250)
        );

        useCase.execute(request);

        verify(repository, times(2))
                .updateBalance(any(Account.class));
    }

    @Test
    void should_return_two_domain_events_after_successful_transfer() {
        Account source = Account.open("Source")
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Account destination = Account.open("Destination");

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.getId().value(),
                destination.getId().value(),
                BigDecimal.valueOf(100)
        );

        UseCaseResult<Void> result = useCase.execute(request);

        assertThat(result.events())
                .hasSize(2);

        assertThat(result.events())
                .allSatisfy(event -> assertThat(event).isNotNull());
    }

    @Test
    void should_not_update_any_account_when_debit_fails() {
        Account source = Account.open("Source");
        Account destination = Account.open("Destination");

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.getId().value(),
                destination.getId().value(),
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(repository, never())
                .updateBalance(any(Account.class));
    }

    @Test
    void should_find_all_accounts_for_update_before_modifying_them() {
        Account source = Account.open("Source")
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Account destination = Account.open("Destination");

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.getId().value(),
                destination.getId().value(),
                BigDecimal.valueOf(100)
        );

        useCase.execute(request);

        InOrder inOrder = inOrder(repository);

        inOrder.verify(repository)
                .findAllForUpdate(anyList());

        inOrder.verify(repository, times(2))
                .updateBalance(any(Account.class));
    }
}