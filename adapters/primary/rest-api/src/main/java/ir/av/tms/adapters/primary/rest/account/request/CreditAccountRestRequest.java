package ir.av.tms.adapters.primary.rest.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Schema
@Setter
@Getter
public class CreditAccountRestRequest {

    @Positive
    private long amount;
}
