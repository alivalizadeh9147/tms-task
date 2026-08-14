package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.TransferMoneyRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.application.usecases.base.UseCase;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.exception.InvalidAccountOperationException;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.event.DomainEvent;
import ir.av.tms.core.domain.shared.vo.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransferMoneyUseCase implements UseCase<TransferMoneyRequest, Void> {

    private final AccountRepository repository;

    public TransferMoneyUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UseCaseResult<Void> execute(TransferMoneyRequest request) {
        AccountId sourceId =
                new AccountId(request.sourceAccountId());

        AccountId destinationId =
                new AccountId(request.destinationAccountId());

        if (sourceId.equals(destinationId)) {
            throw new InvalidAccountOperationException(
                    "Source and destination accounts must be different"
            );
        }

        Money amount = Money.of(request.amount());

        List<AccountId> lockOrder = Stream
                .of(sourceId, destinationId)
                .sorted(Comparator.comparing(AccountId::value))
                .toList();

        Map<AccountId, Account> accounts =
                repository.findAllForUpdate(lockOrder)
                        .stream()
                        .collect(Collectors.toMap(
                                Account::getId,
                                Function.identity()
                        ));

        Account source = Optional
                .ofNullable(accounts.get(sourceId))
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Source account with id '%s' not found"
                                        .formatted(sourceId.value())
                        )
                );

        Account destination = Optional
                .ofNullable(accounts.get(destinationId))
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Destination account with id '%s' not found"
                                        .formatted(destinationId.value())
                        )
                );

        Account withdrawn = source.debit(amount);

        Account deposited = destination.deposit(amount);

        repository.updateBalance(withdrawn);
        repository.updateBalance(deposited);

        List<DomainEvent<?>> events = new ArrayList<>();

        events.addAll(withdrawn.pullDomainEvents());
        events.addAll(deposited.pullDomainEvents());

        return UseCaseResult.of(events);
    }
}
