package ir.av.tms.application.queue;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AccountOperationWorker {

    private final AccountOperationQueue queue;

    public AccountOperationWorker(
            AccountOperationQueue queue
    ) {
        this.queue = queue;
    }

    @PostConstruct
    public void start() {

        for (int i = 0; i < 4; i++) {

            Thread.startVirtualThread(() -> {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        AccountOperation<?> command =
                                queue.take();

                        execute(command);

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    private <T> void execute(
            AccountOperation<T> command
    ) {

        try {

            T result = command.operation().get();

            command.result().complete(result);

        } catch (Exception e) {

            command.result().completeExceptionally(e);
        }
    }
}