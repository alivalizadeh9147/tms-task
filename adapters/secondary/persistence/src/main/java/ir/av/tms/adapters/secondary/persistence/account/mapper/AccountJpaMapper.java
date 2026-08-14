package ir.av.tms.adapters.secondary.persistence.account.mapper;

import ir.av.tms.adapters.secondary.persistence.account.entity.AccountJpaEntity;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class AccountJpaMapper {
    public Account map(AccountJpaEntity entity) {
        return Account.builder()
                .id(new AccountId(entity.getId()))
                .balance(new Money(entity.getBalance()))
                .name(entity.getName())
                .build();
    }

    public AccountJpaEntity map(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(account.getId().value());
        entity.setBalance(account.balance().amount());
        entity.setName(account.name());
        return entity;
    }
}
