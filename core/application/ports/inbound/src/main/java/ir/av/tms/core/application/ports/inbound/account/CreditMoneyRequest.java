package ir.av.tms.core.application.ports.inbound.account;

import ir.av.tms.core.application.ports.inbound.base.BaseRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditMoneyRequest(UUID accountId, BigDecimal amount) implements BaseRequest {
}
