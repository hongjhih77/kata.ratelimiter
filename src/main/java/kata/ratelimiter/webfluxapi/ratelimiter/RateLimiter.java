package kata.ratelimiter.webfluxapi.ratelimiter;

import java.time.Instant;

public abstract class RateLimiter {
    protected final long timeFrameSeconds;
    protected final int permits;

    /**
     * @param timeFrameSeconds the time frame of the permits in seconds
     * @param permits the number of permits
     */
    public RateLimiter(long timeFrameSeconds, int permits) {
        this.timeFrameSeconds = timeFrameSeconds;
        this.permits = permits;
    }

    /**
     * @param currentEpochMilli for testability, the client can designate the Epoch Milli Second.
     * @return {@code true} if the permit was acquired, {@code false} otherwise
     */
    abstract boolean tryAcquire(long currentEpochMilli);

    public boolean tryAcquire(){
        return tryAcquire(Instant.now().toEpochMilli());
    }

    public abstract int getAcquired();
}
