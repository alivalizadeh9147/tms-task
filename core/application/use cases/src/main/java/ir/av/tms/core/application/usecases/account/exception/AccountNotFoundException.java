package ir.av.tms.core.application.usecases.account.exception;

import ir.av.tms.core.domain.account.exception.AccountException;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
