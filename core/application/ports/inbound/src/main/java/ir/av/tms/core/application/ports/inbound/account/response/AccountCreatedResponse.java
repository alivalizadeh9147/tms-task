package ir.av.tms.core.application.ports.inbound.account.response;

import java.util.UUID;

public record AccountCreatedResponse(UUID accountId, String name) {
}
