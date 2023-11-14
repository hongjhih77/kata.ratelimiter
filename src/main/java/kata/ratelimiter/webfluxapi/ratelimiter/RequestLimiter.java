package kata.ratelimiter.webfluxapi.ratelimiter;

public interface RequestLimiter {
    boolean tryAcquire(String rateLimitKey, long timeFrameSeconds, int permits);
}
