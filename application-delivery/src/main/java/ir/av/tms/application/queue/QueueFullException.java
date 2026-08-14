package ir.av.tms.application.queue;

public class QueueFullException extends RuntimeException {
    public QueueFullException(String message) {
        super(message);
    }
}
