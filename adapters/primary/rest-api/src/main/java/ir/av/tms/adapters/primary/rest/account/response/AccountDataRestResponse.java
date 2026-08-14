package ir.av.tms.adapters.primary.rest.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Schema
@Setter
@Getter
public class AccountDataRestResponse {

    private UUID accountId;
    private BigDecimal balance;
    private String name;
}
