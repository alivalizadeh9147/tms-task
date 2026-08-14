package ir.av.tms.core.application.ports.inbound.base;

import ir.av.tms.core.domain.shared.event.DomainEvent;

import java.util.List;

public record UseCaseResult<T>(T data,
                               List<DomainEvent<?>> events) {

    public static <T> UseCaseResult<T> of(List<DomainEvent<?>> events) {
        return new UseCaseResult<>(null, events);
    }
}
