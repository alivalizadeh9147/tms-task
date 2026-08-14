package ir.av.tms.adapters.secondary.persistence.account.adapter;

import ir.av.tms.adapters.secondary.persistence.account.entity.AccountJpaEntity;
import ir.av.tms.adapters.secondary.persistence.account.mapper.AccountJpaMapper;
import ir.av.tms.adapters.secondary.persistence.account.repository.AccountJpaRepository;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.vo.AccountId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    public AccountRepositoryImpl(AccountJpaRepository jpaRepository,
                                 AccountJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Optional<Account> findForUpdate(AccountId id) {
        return jpaRepository.findByIdForUpdate(id.value()).map(mapper::map);
    }

    @Override
    public List<Account> findAllForUpdate(Collection<AccountId> ids) {
        List<UUID> accountIds = ids.stream()
                .map(AccountId::value)
                .toList();

        return jpaRepository
                .findAllByIdForUpdate(accountIds)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public void updateBalance(Account account) {
        int updated = jpaRepository.updateBalance(
                account.getId().value(),
                account.balance().amount()
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Account was not updated: " + account.getId().value()
            );
        }
    }

    @Override
    public void save(Account account) {
        AccountJpaEntity map = mapper.map(account);
        jpaRepository.saveAndFlush(map);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
