package ir.av.tms.adapters.secondary.persistence.base;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@Setter
@Getter
public abstract class JpaBaseEntity {

    @Id
    private UUID id;

    @Version
    private long version;

}
