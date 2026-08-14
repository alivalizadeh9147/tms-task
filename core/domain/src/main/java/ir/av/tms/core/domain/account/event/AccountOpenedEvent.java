package ir.av.tms.core.domain.account.event;

import ir.av.tms.core.domain.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccountOpenedEvent(UUID aggregateId,
                                 UUID eventId,
                                 Instant occurredOn,
                                 Payload payload) implements DomainEvent<AccountOpenedEvent.Payload> {

    public record Payload() {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID aggregateId;
        private UUID eventId;
        private Instant occurredOn;
        private Payload payload;

        private Builder() {
        }

        public Builder aggregateId(UUID aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder occurredOn(Instant occurredOn) {
            this.occurredOn = occurredOn;
            return this;
        }

        public Builder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public AccountOpenedEvent build() {
            return new AccountOpenedEvent(aggregateId, eventId, occurredOn, payload);
        }
    }
}
