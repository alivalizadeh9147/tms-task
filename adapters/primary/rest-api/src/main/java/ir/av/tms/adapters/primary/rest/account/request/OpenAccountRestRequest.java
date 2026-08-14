package ir.av.tms.adapters.primary.rest.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Setter
@Getter
public class OpenAccountRestRequest {

    private String name;
}
