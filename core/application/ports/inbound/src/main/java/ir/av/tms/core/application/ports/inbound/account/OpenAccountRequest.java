package ir.av.tms.core.application.ports.inbound.account;

import ir.av.tms.core.application.ports.inbound.base.BaseRequest;

public record OpenAccountRequest(String name) implements BaseRequest {
}
