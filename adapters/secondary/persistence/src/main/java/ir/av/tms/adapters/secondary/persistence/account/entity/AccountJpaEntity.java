package ir.av.tms.adapters.secondary.persistence.account.entity;

import ir.av.tms.adapters.secondary.persistence.base.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "ACCOUNTS")
@Entity
@Setter
@Getter
public class AccountJpaEntity extends JpaBaseEntity {

    @Column(nullable = false,
            precision = 19,
            scale = 4)
    private BigDecimal balance;

    private String name;
}
