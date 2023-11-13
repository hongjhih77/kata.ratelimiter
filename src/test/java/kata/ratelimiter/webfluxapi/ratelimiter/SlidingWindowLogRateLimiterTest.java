package kata.ratelimiter.webfluxapi.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowLogRateLimiterTest {

    RateLimiter rateLimiter;
    static final long TIME_FRAME_SECOND = 10;
    static final int PERMITS = 2;

    @BeforeEach
    void beforeEach() {
        rateLimiter = new SlidingWindowLogRateLimiter(TIME_FRAME_SECOND, PERMITS);
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds]; " +
            "[When] the acquiring not exceed 2; " +
            "[Then] the acquiring should succeed.")
    void tryAcquire() {

        assertThat(rateLimiter.tryAcquire(1)).isTrue();
        assertThat(rateLimiter.tryAcquire(1)).isTrue();
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds] and 2 existing log; " +
            "[When] the acquiring the third in the same time frame; " +
            "[Then] the acquiring should be failed and the counter of the acquired should be 3.")
    void tryAcquire1() {

        //when
        assertThat(rateLimiter.tryAcquire(1)).isTrue();
        assertThat(rateLimiter.tryAcquire(1)).isTrue();

        //then
        assertThat(rateLimiter.tryAcquire(1)).isFalse();
        assertThat(rateLimiter.getAcquired()).isEqualTo(3);
    }

    @Test
    @DisplayName("[Given] A limiter with rule [2 permit per 10 seconds] and 2 existing log, one of them is outdated; " +
            "[When] the acquiring the third in the same time frame ; " +
            "[Then] the acquiring should succeed and the counter of the acquired should be 2.")
    void tryAcquire2() {

        //when
        assertThat(rateLimiter.tryAcquire(1)).isTrue();
        assertThat(rateLimiter.tryAcquire(2)).isTrue();

        //then
        assertThat(rateLimiter.tryAcquire(1 + TIME_FRAME_SECOND * 1000)).isTrue();
        assertThat(rateLimiter.getAcquired()).isEqualTo(2);
    }
}