package ir.av.tms;

import ir.av.tms.adapters.secondary.persistence.account.repository.AccountJpaRepository;
import ir.av.tms.container.TmsSpringBootApplication;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import ir.av.tms.core.domain.account.entity.Account;
import ir.av.tms.core.domain.account.vo.AccountId;
import ir.av.tms.core.domain.shared.vo.Money;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TmsSpringBootApplication.class)
@Testcontainers
public class AccountRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("account_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @Autowired
    private AccountRepository repository;

    @Autowired
    private AccountJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    public void should_save_and_find_account() {

        Account account = Account.open("Test Account");

        repository.save(account);

        Optional<Account> result =
                repository.findById(account.getId());

        Assertions.assertThat(result)
                .isPresent();

        Account found = result.orElseThrow();

        Assertions.assertThat(found.getId())
                .isEqualTo(account.getId());

        Assertions.assertThat(found.name())
                .isEqualTo("Test Account");

        Assertions.assertThat(found.balance())
                .isEqualTo(Money.zero());
    }

    @Test
    @Transactional
    public void should_find_account_for_update() {

        Account account =
                Account.open("Test Account")
                        .deposit(
                                Money.of(BigDecimal.valueOf(1000))
                        );

        repository.save(account);

        Optional<Account> result =
                repository.findForUpdate(account.getId());

        Assertions.assertThat(result)
                .isPresent();

        Assertions.assertThat(result.orElseThrow().balance())
                .isEqualTo(
                        Money.of(BigDecimal.valueOf(1000))
                );
    }

    @Test
    @Transactional
    public void should_return_empty_when_account_does_not_exist() {

        Optional<Account> result =
                repository.findForUpdate(
                        new AccountId(UUID.randomUUID())
                );

        Assertions.assertThat(result)
                .isEmpty();
    }

    @Test
    @Transactional
    public void should_update_balance_multiple_times() {
        Account account = Account.open("A");

        repository.save(account);

        Account first = account.deposit(
                Money.of(BigDecimal.valueOf(500))
        );

        repository.updateBalance(first);

        Account second = first.deposit(
                Money.of(BigDecimal.valueOf(800))
        );

        repository.updateBalance(second);

        entityManager.clear();

        Account actual = repository.findById(account.getId())
                .orElseThrow();

        assertThat(actual.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(1300)));
    }

    @Test
    @Transactional
    void should_update_account_balance() {

        Account account = Account.open("A");

        repository.save(account);

        Account updated = account.deposit(
                Money.of(BigDecimal.valueOf(500))
        );

        repository.updateBalance(updated);

        entityManager.clear();

        Account actual = repository.findById(account.getId())
                .orElseThrow();

        assertThat(actual.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(500)));
    }
}