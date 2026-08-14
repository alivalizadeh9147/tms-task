package ir.av.tms.core.domain.shared.entity;

import ir.av.tms.core.domain.shared.event.DomainEvent;
import ir.av.tms.core.domain.shared.vo.Identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<ID extends Identity> extends BaseEntity<ID> {

    public AggregateRoot(ID id) {
        super(id);
    }

    private final List<DomainEvent<?>> events = new ArrayList<>();

    protected void addEvent(DomainEvent<?> event) {
        events.add(event);
    }

    public final List<DomainEvent<?>> pullDomainEvents() {
        return Collections.unmodifiableList(events);
    }
}
