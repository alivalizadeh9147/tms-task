package ir.av.tms.core.application.ports.inbound.account;

import ir.av.tms.core.application.ports.inbound.base.BaseRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequest(UUID sourceAccountId,
                                   UUID destinationAccountId,
                                   BigDecimal amount) implements BaseRequest {
}
