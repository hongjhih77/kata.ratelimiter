package kata.ratelimiter.webfluxapi.ratelimiter;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindowLogRateLimiter extends RateLimiter {

    private final ConcurrentLinkedQueue<Long> timeLogQueue = new ConcurrentLinkedQueue<>();

    /**
     * @param timeFrameSeconds the time frame of the permits in seconds
     * @param permits          the number of permits
     */
    public SlidingWindowLogRateLimiter(long timeFrameSeconds, int permits) {
        super(timeFrameSeconds, permits);
    }

    @Override
    boolean tryAcquire(long currentEpochMilli) {
        timeLogQueue.add(currentEpochMilli);
        if(timeLogQueue.size() > permits){
            removeOutDated(currentEpochMilli);
        }
        return timeLogQueue.size() <= permits;
    }

    @Override
    public int getAcquired() {
        return timeLogQueue.size();
    }

    private synchronized void removeOutDated(long currentEpochMilli) {
        while (timeLogQueue.peek() != null && (currentEpochMilli - timeLogQueue.peek()) >= timeFrameSeconds * 1000) {
            timeLogQueue.poll();
        }
    }
}
