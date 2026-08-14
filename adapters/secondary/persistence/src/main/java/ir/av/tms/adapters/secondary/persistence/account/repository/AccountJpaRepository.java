package ir.av.tms.adapters.secondary.persistence.account.repository;

import ir.av.tms.adapters.secondary.persistence.account.entity.AccountJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from AccountJpaEntity a
        where a.id = :id
    """)
    Optional<AccountJpaEntity> findByIdForUpdate(
            @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from AccountJpaEntity a
        where a.id in :ids
        order by a.id
    """)
    List<AccountJpaEntity> findAllByIdForUpdate(
            @Param("ids") Collection<UUID> ids
    );

    @Modifying
    @Query("""
    update AccountJpaEntity a
       set a.balance = :balance,
           a.version = a.version + 1
     where a.id = :id
""")
    int updateBalance(
            @Param("id") UUID id,
            @Param("balance") BigDecimal balance
    );
}
