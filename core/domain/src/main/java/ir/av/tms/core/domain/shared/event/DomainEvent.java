package ir.av.tms.core.domain.shared.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent<P> {

    UUID aggregateId();

    UUID eventId();

    Instant occurredOn();

    P payload();
}
