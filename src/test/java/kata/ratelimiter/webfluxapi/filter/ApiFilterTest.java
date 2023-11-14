package kata.ratelimiter.webfluxapi.filter;

import kata.ratelimiter.webfluxapi.ratelimiter.RequestLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApiFilterTest {

    @Mock
    RequestLimiter requestLimiter;

    @Test
    @DisplayName("[Given] A mock request limiter, a mock request; " +
            "[When] Exceeded to the limit;" +
            "[Then] Return an HTTP 429 status (Too Many Requests).")
    void filter() throws UnknownHostException {

        //given A mock request limiter, a mock request
        int permits = 2;
        int timeFrameSeconds = 10;
        ApiFilter filter = new ApiFilter(requestLimiter, timeFrameSeconds, permits);
        WebFilterChain filterChain = filterExchange -> Mono.empty();
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .get("http://localhost/api/resource")
                        .remoteAddress(new InetSocketAddress(addr, 8080)));

        var sourceIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        var uri = exchange.getRequest().getURI();
        var path = uri.getPath();
        String rateLimitKey = getKeyOfRateLimiter(sourceIP, path);

        Mockito.when(requestLimiter.tryAcquire(rateLimitKey, 10, 2)).thenReturn(false);

        //when exceeded to the limit
        filter.filter(exchange, filterChain).block();
        //then return an HTTP 429 status (Too Many Requests).
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    private String getKeyOfRateLimiter(String ip, String path) {
        return path + ";" + ip;
    }
}