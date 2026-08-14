package ir.av.tms.core.domain.shared.entity;

import ir.av.tms.core.domain.shared.vo.Identity;

public abstract class BaseEntity<ID extends Identity> {

    private final ID id;

    public BaseEntity(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }
}
