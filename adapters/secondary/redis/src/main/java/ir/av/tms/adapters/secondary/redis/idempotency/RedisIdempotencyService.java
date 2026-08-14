package ir.av.tms.adapters.secondary.redis.idempotency;

import ir.av.tms.application.idempotency.IdempotencyService;
import ir.av.tms.application.idempotency.exception.DuplicateIdempotencyKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisIdempotencyService implements IdempotencyService {

    private static final String PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyService(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void execute(
            String idempotencyKey,
            Runnable action
    ) {

        String key = PREFIX + idempotencyKey;

        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            throw new DuplicateIdempotencyKeyException("There is already an idempotency key '" + key + "'");
        }

        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        "PROCESSED",
                        TTL
                );

        if (!Boolean.TRUE.equals(created)) {
            return;
        }

        try {
            action.run();
        } catch (RuntimeException e) {
            redisTemplate.delete(key);
            throw e;
        }
    }
}