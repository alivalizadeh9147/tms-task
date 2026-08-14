package ir.av.tms.core.application.ports.outbound.repository.account;

import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.vo.AccountId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(AccountId id);

    Optional<Account> findForUpdate(AccountId id);

    List<Account> findAllForUpdate(Collection<AccountId> ids);

    void updateBalance(Account account);

    void save(Account account);

    void deleteAll();
}
