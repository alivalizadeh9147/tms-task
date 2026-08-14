package ir.av.tms.adapters.primary.rest.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema
@Setter
@Getter
public class TransferAccountRestRequest {

    @NotNull
    private UUID destinationAccountId;
    @Positive
    private long amount;
}
