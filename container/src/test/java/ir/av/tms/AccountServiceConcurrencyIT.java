package ir.av.tms;

import ir.av.tms.application.delivery.AccountService;
import ir.av.tms.application.delivery.dto.AccountDto;
import ir.av.tms.container.TmsSpringBootApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TmsSpringBootApplication.class)
@Testcontainers
class AccountServiceConcurrencyIT {

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


    // ---------------------------------------------------------
    // Concurrent Credit
    // ---------------------------------------------------------

    @Test
    void should_handle_concurrent_credits()
            throws Exception {

        UUID accountId =
                accountService.openAccount("A");

        int threadCount = 100;
        long amount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.credit(
                                accountId,
                                amount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("10000");
    }


    // ---------------------------------------------------------
    // Concurrent Debit
    // ---------------------------------------------------------

    @Test
    void should_handle_concurrent_debits()
            throws Exception {

        UUID accountId =
                accountService.openAccount("A");

        int threadCount = 100;
        long amount = 100;

        accountService.credit(
                accountId,
                threadCount * amount,
                UUID.randomUUID().toString()
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.debit(
                                accountId,
                                amount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("0");
    }


    // ---------------------------------------------------------
    // Concurrent Credit + Debit
    // ---------------------------------------------------------

    @Test
    void should_handle_concurrent_credit_and_debit()
            throws Exception {

        UUID accountId =
                accountService.openAccount("A");

        int creditThreads = 50;
        int debitThreads = 50;

        long creditAmount = 100;
        long debitAmount = 100;

        accountService.credit(
                accountId,
                5000,
                UUID.randomUUID().toString()
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(100);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < creditThreads; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.credit(
                                accountId,
                                creditAmount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        for (int i = 0; i < debitThreads; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.debit(
                                accountId,
                                debitAmount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("5000");
    }


    // ---------------------------------------------------------
    // Concurrent Transfer A -> B
    // ---------------------------------------------------------

    @Test
    void should_handle_concurrent_transfers_in_same_direction()
            throws Exception {

        UUID source =
                accountService.openAccount("SOURCE");

        UUID destination =
                accountService.openAccount("DESTINATION");

        int threadCount = 100;
        long amount = 100;

        accountService.credit(
                source,
                threadCount * amount,
                UUID.randomUUID().toString()
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.transfer(
                                source,
                                destination,
                                amount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto sourceAccount =
                accountService.get(source);

        AccountDto destinationAccount =
                accountService.get(destination);

        assertThat(sourceAccount.balance())
                .isEqualByComparingTo("0");

        assertThat(destinationAccount.balance())
                .isEqualByComparingTo("10000");
    }


    // ---------------------------------------------------------
    // Concurrent Transfer A -> B and B -> A
    // ---------------------------------------------------------

    @Test
    void should_handle_bidirectional_concurrent_transfers()
            throws Exception {

        UUID accountA =
                accountService.openAccount("A");

        UUID accountB =
                accountService.openAccount("B");

        int transferCount = 100;
        long amount = 100;

        accountService.credit(
                accountA,
                10000,
                UUID.randomUUID().toString()
        );

        accountService.credit(
                accountB,
                10000,
                UUID.randomUUID().toString()
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(100);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < transferCount / 2; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.transfer(
                                accountA,
                                accountB,
                                amount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        for (int i = 0; i < transferCount / 2; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        accountService.transfer(
                                accountB,
                                accountA,
                                amount,
                                UUID.randomUUID().toString()
                        );

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto finalA =
                accountService.get(accountA);

        AccountDto finalB =
                accountService.get(accountB);

        assertThat(finalA.balance())
                .isEqualByComparingTo("10000");

        assertThat(finalB.balance())
                .isEqualByComparingTo("10000");
    }


    // ---------------------------------------------------------
    // Same Idempotency Key
    // ---------------------------------------------------------

    @Test
    void should_execute_credit_only_once_with_same_key()
            throws Exception {

        UUID accountId =
                accountService.openAccount("A");

        String idempotencyKey =
                UUID.randomUUID().toString();

        int threadCount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        try {

                            accountService.credit(
                                    accountId,
                                    100,
                                    idempotencyKey
                            );

                        } catch (CompletionException ignored) {
                            // expected
                        }

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("100");
    }


    // ---------------------------------------------------------
    // Failed Debit must not change balance
    // ---------------------------------------------------------

    @Test
    void should_not_change_balance_when_concurrent_debit_fails()
            throws Exception {

        UUID accountId =
                accountService.openAccount("A");

        accountService.credit(
                accountId,
                1000,
                UUID.randomUUID().toString()
        );

        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        try {

                            accountService.debit(
                                    accountId,
                                    1000,
                                    UUID.randomUUID().toString()
                            );

                        } catch (CompletionException ignored) {
                            // expected for all but one request
                        }

                        return null;
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        AccountDto account =
                accountService.get(accountId);

        assertThat(account.balance())
                .isEqualByComparingTo("0");
    }
}