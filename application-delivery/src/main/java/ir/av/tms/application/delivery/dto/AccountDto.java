package ir.av.tms.application.delivery.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(UUID id, String name, BigDecimal balance) {
}
