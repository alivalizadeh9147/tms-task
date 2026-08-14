package ir.av.tms.application.delivery;

import ir.av.tms.application.delivery.dto.AccountDto;

import java.util.UUID;

public interface AccountService {

    UUID openAccount(String name);

    void credit(UUID accountId,
                long amount,
                String transactionId);

    void debit(UUID accountId,
               long amount,
               String transactionId);

    void transfer(UUID sourceAccountId,
                  UUID destinationAccountId,
                  long amount,
                  String transactionId);

    AccountDto get(UUID accountId);
}
