package ir.av.tms.core.domain.account.exception;

import ir.av.tms.core.domain.shared.exception.DomainException;

public class AccountException extends DomainException {
    public AccountException(String message) {
        super(message);
    }
}
