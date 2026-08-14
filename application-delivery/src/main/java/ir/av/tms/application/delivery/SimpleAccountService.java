package ir.av.tms.application.delivery;

import ir.av.tms.application.delivery.dto.AccountDto;
import ir.av.tms.application.idempotency.IdempotencyService;
import ir.av.tms.application.queue.AccountOperationQueue;
import ir.av.tms.core.application.ports.inbound.account.CreditMoneyRequest;
import ir.av.tms.core.application.ports.inbound.account.DebitMoneyRequest;
import ir.av.tms.core.application.ports.inbound.account.OpenAccountRequest;
import ir.av.tms.core.application.ports.inbound.account.TransferMoneyRequest;
import ir.av.tms.core.application.ports.inbound.account.response.AccountCreatedResponse;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.CreditMoneyUseCase;
import ir.av.tms.core.application.usecases.account.DebitMoneyUseCase;
import ir.av.tms.core.application.usecases.account.OpenAccountUseCase;
import ir.av.tms.core.application.usecases.account.TransferMoneyUseCase;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.domain.account.vo.AccountId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class SimpleAccountService implements AccountService {

    private final AccountOperationQueue queue;

    private final AccountRepository accountRepository;
    private final OpenAccountUseCase openAccountUseCase;
    private final CreditMoneyUseCase creditMoneyUseCase;
    private final DebitMoneyUseCase debitMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final IdempotencyService idempotencyService;

    public SimpleAccountService(
            AccountOperationQueue queue,
            OpenAccountUseCase openAccountUseCase,
            CreditMoneyUseCase creditMoneyUseCase,
            DebitMoneyUseCase debitMoneyUseCase,
            TransferMoneyUseCase transferMoneyUseCase,
            AccountRepository accountRepository,
            IdempotencyService idempotencyService
    ) {
        this.queue = queue;
        this.openAccountUseCase = openAccountUseCase;
        this.creditMoneyUseCase = creditMoneyUseCase;
        this.debitMoneyUseCase = debitMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.accountRepository = accountRepository;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public UUID openAccount(String name) {

        UseCaseResult<AccountCreatedResponse> result =
                openAccountUseCase.execute(
                        new OpenAccountRequest(name)
                );

        return result.data().accountId();
    }

    @Override
    public void credit(
            UUID accountId,
            long amount,
            String idempotencyKey
    ) {
        CompletableFuture<Void> future =
                queue.submit(() -> {
                            idempotencyService.execute(
                                    idempotencyKey,
                                    () -> creditMoneyUseCase.execute(
                                            new CreditMoneyRequest(
                                                    accountId,
                                                    BigDecimal.valueOf(amount)
                                            )
                                    )
                            );
                            return null;
                        }
                );
        future.join();

    }

    @Override
    public void debit(
            UUID accountId,
            long amount,
            String idempotencyKey
    ) {
        CompletableFuture<Void> future =
                queue.submit(() -> {
                    idempotencyService.execute(
                            idempotencyKey,
                            () -> debitMoneyUseCase.execute(
                                    new DebitMoneyRequest(
                                            accountId,
                                            BigDecimal.valueOf(amount)
                                    )
                            )
                    );
                    return null;
                });
        future.join();
    }

    @Override
    public void transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount,
            String idempotencyKey
    ) {

        CompletableFuture<Void> future =
                queue.submit(() -> {
                            idempotencyService.execute(
                                    idempotencyKey,
                                    () -> transferMoneyUseCase.execute(
                                            new TransferMoneyRequest(
                                                    sourceAccountId,
                                                    destinationAccountId,
                                                    BigDecimal.valueOf(amount)
                                            )
                                    )
                            );
                            return null;
                        }
                );
        future.join();

    }

    @Override
    public AccountDto get(UUID accountId) {

        return accountRepository.findById(
                        new AccountId(accountId)
                )
                .map(account ->
                        new AccountDto(
                                accountId,
                                account.name(),
                                account.balance().amount()
                        )
                )
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account with id '%s' not found"
                                        .formatted(accountId)
                        )
                );
    }
}