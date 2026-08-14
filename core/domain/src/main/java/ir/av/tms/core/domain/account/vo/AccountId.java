package ir.av.tms.core.domain.account.vo;

import ir.av.tms.core.domain.account.exception.InvalidAccountOperationException;
import ir.av.tms.core.domain.shared.vo.Identity;

import java.util.UUID;

public record AccountId(UUID value) implements Identity {

    public AccountId {
        if (value == null) {
            throw new InvalidAccountOperationException("Account Id Not Valid");
        }
    }

    public static AccountId open() {
        return new AccountId(UUID.randomUUID());
    }
}
