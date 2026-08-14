package ir.av.tms.application.idempotency;

public interface IdempotencyService {

    void execute(
            String idempotencyKey,
            Runnable action
    );
}
