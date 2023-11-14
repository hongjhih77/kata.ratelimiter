package kata.ratelimiter.webfluxapi.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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

    @RepeatedTest(3)
    @DisplayName("Concurrency Test")
    void tryAcquireConcurrency() throws InterruptedException {
        InMemoryRequestLimiter requestLimiter = new InMemoryRequestLimiter();

        var key = "key";
        int timeFrameSeconds = 60;
        int permits = 100;

        AtomicBoolean t1Acquired = new AtomicBoolean(true);
        var t1 = new Thread(() -> {
            for (int i = 1; i <= 50; i++) {
                if (!requestLimiter.tryAcquire(key, timeFrameSeconds, permits)) {
                    t1Acquired.set(false);
                }
            }
        });

        AtomicBoolean t2Acquired = new AtomicBoolean(true);
        var t2 = new Thread(() -> {
            for (int i = 1; i <= 50; i++) {
                if (!requestLimiter.tryAcquire(key, timeFrameSeconds, permits)) {
                    t2Acquired.set(false);
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        //check the requests from [1..100] are all succeed.
        assertThat(t1Acquired.get() && t2Acquired.get()).isTrue();
        //The last request should be failed.
        assertThat(requestLimiter.tryAcquire(key, timeFrameSeconds, permits)).isFalse();
    }
}