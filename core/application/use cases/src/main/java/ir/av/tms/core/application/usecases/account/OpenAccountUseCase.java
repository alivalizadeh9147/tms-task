package ir.av.tms.core.application.usecases.account;

import ir.av.tms.core.application.ports.inbound.account.OpenAccountRequest;
import ir.av.tms.core.application.ports.inbound.account.response.AccountCreatedResponse;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.application.usecases.base.UseCase;
import ir.av.tms.core.domain.account.entity.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenAccountUseCase implements UseCase<OpenAccountRequest, AccountCreatedResponse> {

    private final AccountRepository repository;

    public OpenAccountUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UseCaseResult<AccountCreatedResponse> execute(OpenAccountRequest request) {
        Account account = Account.open(request.name());

        repository.save(account);

        AccountCreatedResponse response = new AccountCreatedResponse(account.getId().value(), account.name());

        return new UseCaseResult<>(response, account.pullDomainEvents());
    }
}
