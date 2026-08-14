package ir.av.tms;

import ir.av.tms.application.delivery.AccountService;
import ir.av.tms.application.delivery.dto.AccountDto;
import ir.av.tms.container.TmsSpringBootApplication;
import ir.av.tms.core.application.ports.outbound.repository.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TmsSpringBootApplication.class)
@Testcontainers
class AccountServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

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

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void should_open_account() {

        UUID accountId =
                accountService.openAccount("A");

        assertThat(accountId)
                .isNotNull();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.id())
                .isEqualTo(accountId);

        assertThat(account.name())
                .isEqualTo("A");

        assertThat(account.balance())
                .isEqualByComparingTo("0");
    }

    @Test
    void should_get_account() {

        UUID accountId =
                accountService.openAccount("A");

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.id())
                .isEqualTo(accountId);

        assertThat(account.name())
                .isEqualTo("A");

        assertThat(account.balance())
                .isEqualByComparingTo("0");
    }

    @Test
    void should_credit_account() {

        UUID accountId =
                accountService.openAccount("A");

        String idempotencyKey =
                UUID.randomUUID().toString();

        accountService.credit(
                accountId,
                1000,
                idempotencyKey
        );

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("1000");
    }

    @Test
    void should_debit_account() {

        UUID accountId =
                accountService.openAccount("A");

        accountService.credit(
                accountId,
                1000,
                UUID.randomUUID().toString()
        );

        accountService.debit(
                accountId,
                400,
                UUID.randomUUID().toString()
        );

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("600");
    }

    @Test
    void should_transfer_money() {

        UUID source =
                accountService.openAccount("SOURCE");

        UUID destination =
                accountService.openAccount("DESTINATION");

        accountService.credit(
                source,
                1000,
                UUID.randomUUID().toString()
        );

        accountService.transfer(
                source,
                destination,
                400,
                UUID.randomUUID().toString()
        );

        AccountDto sourceAccount =
                accountService.get(source);

        AccountDto destinationAccount =
                accountService.get(destination);

        assertThat(sourceAccount.balance())
                .isEqualByComparingTo("600");

        assertThat(destinationAccount.balance())
                .isEqualByComparingTo("400");
    }

    @Test
    void should_not_credit_twice_with_same_idempotency_key() {

        UUID accountId =
                accountService.openAccount("A");

        String key =
                UUID.randomUUID().toString();

        accountService.credit(
                accountId,
                1000,
                key
        );

        assertThatThrownBy(() ->
                accountService.credit(
                        accountId,
                        1000,
                        key
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("1000");
    }

    @Test
    void should_execute_operations_with_different_idempotency_keys() {

        UUID accountId =
                accountService.openAccount("A");

        accountService.credit(
                accountId,
                1000,
                UUID.randomUUID().toString()
        );

        accountService.credit(
                accountId,
                500,
                UUID.randomUUID().toString()
        );

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("1500");
    }

    @Test
    void should_not_transfer_twice_with_same_idempotency_key() {

        UUID source =
                accountService.openAccount("SOURCE");

        UUID destination =
                accountService.openAccount("DESTINATION");

        accountService.credit(
                source,
                1000,
                UUID.randomUUID().toString()
        );

        String key =
                UUID.randomUUID().toString();

        accountService.transfer(
                source,
                destination,
                400,
                key
        );

        assertThatThrownBy(() ->
                accountService.transfer(
                        source,
                        destination,
                        400,
                        key
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        AccountDto sourceAccount =
                accountService.get(source);

        AccountDto destinationAccount =
                accountService.get(destination);

        assertThat(sourceAccount.balance())
                .isEqualByComparingTo("600");

        assertThat(destinationAccount.balance())
                .isEqualByComparingTo("400");
    }

    @Test
    void should_reject_debit_when_balance_is_insufficient() {

        UUID accountId =
                accountService.openAccount("A");

        accountService.credit(
                accountId,
                500,
                UUID.randomUUID().toString()
        );

        assertThatThrownBy(() ->
                accountService.debit(
                        accountId,
                        1000,
                        UUID.randomUUID().toString()
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("500");
    }

    @Test
    void should_not_change_balances_when_transfer_fails() {

        UUID source =
                accountService.openAccount("SOURCE");

        UUID destination =
                accountService.openAccount("DESTINATION");

        accountService.credit(
                source,
                500,
                UUID.randomUUID().toString()
        );

        assertThatThrownBy(() ->
                accountService.transfer(
                        source,
                        destination,
                        1000,
                        UUID.randomUUID().toString()
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        AccountDto sourceAccount =
                accountService.get(source);

        AccountDto destinationAccount =
                accountService.get(destination);

        assertThat(sourceAccount.balance())
                .isEqualByComparingTo("500");

        assertThat(destinationAccount.balance())
                .isEqualByComparingTo("0");
    }
}
