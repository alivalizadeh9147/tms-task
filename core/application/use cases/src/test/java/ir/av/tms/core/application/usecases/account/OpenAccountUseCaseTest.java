package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.OpenAccountRequest;
import ir.av.tms.core.application.ports.inbound.account.response.AccountCreatedResponse;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.event.AccountOpenedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAccountUseCaseTest {

    @Mock
    private AccountRepository repository;

    private OpenAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OpenAccountUseCase(repository);
    }

    @Test
    void shouldOpenAccountSuccessfully() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.data());

        assertEquals(
                "Ali",
                result.data().name()
        );

        assertNotNull(
                result.data().accountId()
        );

        verify(repository).save(any(Account.class));
    }

    @Test
    void shouldSaveOpenedAccount() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        useCase.execute(request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(repository).save(captor.capture());

        Account savedAccount = captor.getValue();

        assertNotNull(savedAccount);
        assertNotNull(savedAccount.getId());

        assertEquals(
                "Ali",
                savedAccount.name()
        );

        assertEquals(
                0,
                savedAccount.balance().amount().signum()
        );
    }

    @Test
    void shouldReturnCreatedAccountId() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(repository).save(captor.capture());

        Account savedAccount = captor.getValue();

        assertEquals(
                savedAccount.getId().value(),
                result.data().accountId()
        );
    }

    @Test
    void shouldReturnAccountName() {
        OpenAccountRequest request =
                new OpenAccountRequest("My Account");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        assertEquals(
                "My Account",
                result.data().name()
        );
    }

    @Test
    void shouldReturnAccountOpenedEvent() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result.events());

        assertEquals(
                1,
                result.events().size()
        );

        assertInstanceOf(
                AccountOpenedEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldUseCreatedAccountIdInAccountOpenedEvent() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        AccountOpenedEvent event =
                (AccountOpenedEvent) result.events().getFirst();

        assertEquals(
                result.data().accountId(),
                event.aggregateId()
        );
    }

    @Test
    void shouldNotReturnNullEvents() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        UseCaseResult<AccountCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result.events());
    }

    @Test
    void shouldSaveOnlyOnce() {
        OpenAccountRequest request =
                new OpenAccountRequest("Ali");

        useCase.execute(request);

        verify(repository, times(1))
                .save(any(Account.class));
    }
}