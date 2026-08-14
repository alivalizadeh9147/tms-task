package ir.av.tms.core.application.usecases.base;

import ir.av.tms.core.application.ports.inbound.base.BaseRequest;
import ir.av.tms.core.application.ports.inbound.base.UseCaseResult;

public interface UseCase<REQUEST extends BaseRequest, RESPONSE> {

    UseCaseResult<RESPONSE> execute(REQUEST request);
}
