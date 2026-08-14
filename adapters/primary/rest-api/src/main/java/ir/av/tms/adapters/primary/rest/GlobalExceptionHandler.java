package ir.av.tms.adapters.primary.rest;

import ir.av.tms.adapters.primary.rest.base.BaseResponse;
import ir.av.tms.adapters.primary.rest.base.ErrorItem;
import ir.av.tms.application.idempotency.exception.DuplicateIdempotencyKeyException;
import ir.av.tms.core.application.usecases.account.exception.AccountNotFoundException;
import ir.av.tms.core.domain.shared.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = DomainException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<BaseResponse<?>> handleException(DomainException e) {
        ErrorItem item = new ErrorItem(e.getMessage());
        return ResponseEntity.unprocessableEntity().body(BaseResponse.error(item));
    }

    @ExceptionHandler(exception = CompletionException.class)
    public ResponseEntity<BaseResponse<?>> handleException(CompletionException e) {
        Throwable cause = e.getCause();
        ErrorItem item = new ErrorItem(cause.getMessage());
        if (cause instanceof DuplicateIdempotencyKeyException) {
            return ResponseEntity.status(409).body(BaseResponse.error(item));
        }
        if (cause instanceof AccountNotFoundException) {
            return ResponseEntity.status(404).body(BaseResponse.error(item));
        }
        return ResponseEntity.internalServerError().body(BaseResponse.error(item));
    }

    @ExceptionHandler(exception = DuplicateIdempotencyKeyException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<BaseResponse<?>> handleException(DuplicateIdempotencyKeyException e) {
        ErrorItem item = new ErrorItem(e.getMessage());
        return ResponseEntity.status(409).body(BaseResponse.error(item));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<BaseResponse<?>> handleException(RuntimeException e) {
        ErrorItem item = new ErrorItem(e.getMessage());
        return ResponseEntity.internalServerError().body(BaseResponse.error(item));
    }
}
