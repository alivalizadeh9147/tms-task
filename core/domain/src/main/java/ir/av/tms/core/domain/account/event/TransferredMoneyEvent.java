package ir.av.tms.core.domain.account.event;

import ir.av.tms.core.domain.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record TransferredMoneyEvent(UUID aggregateId,
                                    UUID eventId,
                                    Instant occurredOn,
                                    Payload payload) implements DomainEvent<TransferredMoneyEvent.Payload> {


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

        public TransferredMoneyEvent build() {
            return new TransferredMoneyEvent(aggregateId, eventId, occurredOn, payload);
        }
    }
}
