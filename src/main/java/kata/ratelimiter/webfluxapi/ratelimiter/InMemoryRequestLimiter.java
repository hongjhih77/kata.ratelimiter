package kata.ratelimiter.webfluxapi.ratelimiter;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("ratelimit-in-mem")
@Primary
public class InMemoryRequestLimiter implements RequestLimiter {

    Map<String, RateLimiter> rateLimitByKey = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String rateLimitKey, long timeFrameSeconds, int permits) {
        rateLimitByKey.putIfAbsent(rateLimitKey,
                new SlidingWindowLogRateLimiter(timeFrameSeconds, permits));
        RateLimiter rateLimiter = rateLimitByKey.get(rateLimitKey);
        return rateLimiter.tryAcquire();
    }

}
