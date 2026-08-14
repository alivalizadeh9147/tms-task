package ir.av.tms.application.queue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record AccountOperation<T>(
        Supplier<T> operation,
        CompletableFuture<T> result
) {

    public static <T> AccountOperation<T> of(
            Supplier<T> operation
    ) {
        return new AccountOperation<>(
                operation,
                new CompletableFuture<>()
        );
    }
}