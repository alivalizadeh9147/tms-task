package ir.av.tms.core.domain.shared.exception;

import ir.av.tms.core.domain.account.vo.AccountId;

public final class InsufficientBalanceException
        extends DomainException {

    public InsufficientBalanceException(
            AccountId accountId
    ) {
        super(
                "Insufficient balance for account: " + accountId
        );
    }
}