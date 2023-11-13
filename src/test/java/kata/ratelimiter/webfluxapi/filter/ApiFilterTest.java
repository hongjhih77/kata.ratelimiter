package kata.ratelimiter.webfluxapi.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiFilterTest {
    @Test
    @DisplayName("[Given] a 2 request per 10 seconds rate; " +
            "[When] acquire permissions to the limit and acquire once again;" +
            "[Then] the limit is exceeded, return an HTTP 429 status (Too Many Requests).")
    void filter() throws UnknownHostException {
        //given a 2 request per 10 seconds rate
        int permits = 2;
        int timeFrameSeconds = 10;
        ApiFilter filter = new ApiFilter(timeFrameSeconds, permits);
        WebFilterChain filterChain = filterExchange -> Mono.empty();
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .get("http://localhost/api/resource")
                        .remoteAddress(new InetSocketAddress(addr, 8080)));
        //when acquire permissions to the limit and acquire once again
        for (int i = 1; i <= permits; i++) {
            filter.filter(exchange, filterChain).block();
        }
        filter.filter(exchange, filterChain).block();
        //then the limit is exceeded, return an HTTP 429 status (Too Many Requests).
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}