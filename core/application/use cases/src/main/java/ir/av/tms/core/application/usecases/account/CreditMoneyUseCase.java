package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.CreditMoneyRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.application.usecases.base.UseCase;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.vo.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditMoneyUseCase implements UseCase<CreditMoneyRequest, Void> {

    private final AccountRepository repository;

    public CreditMoneyUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UseCaseResult<Void> execute(CreditMoneyRequest request) {
        Account account = repository.findForUpdate(
                new AccountId(request.accountId())
        ).orElseThrow(() ->
                new AccountNotFoundException(
                        "Account with id '%s' not found"
                                .formatted(request.accountId())
                )
        );

        Account credited = account.deposit(
                Money.of(request.amount())
        );

        repository.updateBalance(credited);

        return UseCaseResult.of(credited.pullDomainEvents());
    }
}
