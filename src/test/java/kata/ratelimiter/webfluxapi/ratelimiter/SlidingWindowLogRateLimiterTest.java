package kata.ratelimiter.webfluxapi.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlidingWindowLogRateLimiterTest {

    RateLimiter rateLimiter;
    static final long TIME_FRAME_SECOND = 10;
    static final int PERMITS = 2;

    @BeforeEach
    void beforeEach(){
        rateLimiter = new SlidingWindowLogRateLimiter(TIME_FRAME_SECOND, PERMITS);
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds]; " +
            "[When] the acquiring not exceed 2; " +
            "[Then] the acquiring should succeed.")
    void tryAcquire() {

        Assertions.assertTrue(rateLimiter.tryAcquire(1));
        Assertions.assertTrue(rateLimiter.tryAcquire(1));
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds] and 2 existing log; " +
            "[When] the acquiring the third in the same time frame; " +
            "[Then] the acquiring should be failed and the counter of the acquired should be 3.")
    void tryAcquire1() {

        //when
        Assertions.assertTrue(rateLimiter.tryAcquire(1));
        Assertions.assertTrue(rateLimiter.tryAcquire(1));

        //then
        Assertions.assertFalse(rateLimiter.tryAcquire(1));
        Assertions.assertEquals(3, rateLimiter.getAcquired());
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds] and 2 existing log, one of them is outdated; " +
            "[When] the acquiring the third in the same time frame ; " +
            "[Then] the acquiring should succeed and the counter of the acquired should be 2.")
    void tryAcquire2() {

        //when
        Assertions.assertTrue(rateLimiter.tryAcquire(1));
        Assertions.assertTrue(rateLimiter.tryAcquire(2));

        //then
        Assertions.assertTrue(rateLimiter.tryAcquire(1 + TIME_FRAME_SECOND * 1000));
        Assertions.assertEquals(2, rateLimiter.getAcquired());
    }
}