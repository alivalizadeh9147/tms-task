package ir.av.tms.application.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
public class AccountOperationQueue {

    private final BlockingQueue<AccountOperation<?>> queue =
            new ArrayBlockingQueue<>(100_000);

    public <T> CompletableFuture<T> submit(
            Supplier<T> operation
    ) {

        AccountOperation<T> command =
                AccountOperation.of(operation);

        boolean accepted = queue.offer(command);

        if (!accepted) {
            throw new QueueFullException(
                    "Account operation queue is full"
            );
        }

        return command.result();
    }

    public AccountOperation<?> take()
            throws InterruptedException {

        return queue.take();
    }
}