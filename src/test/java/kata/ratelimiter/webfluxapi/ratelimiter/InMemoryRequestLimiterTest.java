package kata.ratelimiter.webfluxapi.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRequestLimiterTest {
    @Test
    @DisplayName("[Given] A InMemoryRequestLimiter; " +
            "[When] Acquire permissions to the limit and acquire once again;" +
            "[Then] Return false for the last acquiring.")
    void tryAcquire() {
        InMemoryRequestLimiter requestLimiter = new InMemoryRequestLimiter();
        var key = "key";

        int timeFrameSeconds = 10;
        int permits = 2;
        assertThat(requestLimiter.tryAcquire(key, timeFrameSeconds, permits)).isTrue();
        assertThat(requestLimiter.tryAcquire(key, timeFrameSeconds, permits)).isTrue();

        assertThat(requestLimiter.tryAcquire(key, timeFrameSeconds, permits)).isFalse();
    }
}