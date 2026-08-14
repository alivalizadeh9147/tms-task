package ir.av.tms;

import ir.av.tms.application.idempotency.IdempotencyService;
import ir.av.tms.application.idempotency.exception.DuplicateIdempotencyKeyException;
import ir.av.tms.container.TmsSpringBootApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TmsSpringBootApplication.class)
@Testcontainers
class IdempotencyIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedis(
            DynamicPropertyRegistry registry
    ) {
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
    private IdempotencyService idempotencyService;

    @Test
    void should_reject_duplicate_idempotency_key() {

        String key = UUID.randomUUID().toString();

        AtomicInteger counter = new AtomicInteger();

        idempotencyService.execute(
                key,
                counter::incrementAndGet
        );

        assertThatThrownBy(() ->
                idempotencyService.execute(
                        key,
                        counter::incrementAndGet
                )
        )
                .isInstanceOf(DuplicateIdempotencyKeyException.class);

        assertThat(counter.get())
                .isEqualTo(1);
    }

    @Test
    void should_execute_operations_with_different_keys() {

        AtomicInteger counter = new AtomicInteger();

        idempotencyService.execute(
                UUID.randomUUID().toString(),
                counter::incrementAndGet
        );

        idempotencyService.execute(
                UUID.randomUUID().toString(),
                counter::incrementAndGet
        );

        assertThat(counter.get())
                .isEqualTo(2);
    }

    @Test
    void should_allow_retry_when_action_fails() {

        String key = UUID.randomUUID().toString();

        AtomicInteger counter = new AtomicInteger();

        assertThatThrownBy(() ->
                idempotencyService.execute(
                        key,
                        () -> {
                            counter.incrementAndGet();
                            throw new RuntimeException("Something failed");
                        }
                )
        ).isInstanceOf(RuntimeException.class);

        idempotencyService.execute(
                key,
                counter::incrementAndGet
        );

        assertThat(counter.get())
                .isEqualTo(2);
    }

    @Test
    void should_allow_only_one_execution_concurrently()
            throws Exception {

        String key = UUID.randomUUID().toString();

        AtomicInteger counter = new AtomicInteger();

        int threadCount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(() -> {

                        try {
                            startLatch.await();

                            idempotencyService.execute(
                                    key,
                                    () -> {
                                        counter.incrementAndGet();

                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread()
                                                    .interrupt();
                                        }
                                    }
                            );

                        } catch (DuplicateIdempotencyKeyException ignored) {
                            // Expected for concurrent duplicate requests

                        } catch (InterruptedException e) {
                            Thread.currentThread()
                                    .interrupt();

                            throw new RuntimeException(e);
                        }
                    })
            );
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        assertThat(counter.get())
                .isEqualTo(1);
    }
}