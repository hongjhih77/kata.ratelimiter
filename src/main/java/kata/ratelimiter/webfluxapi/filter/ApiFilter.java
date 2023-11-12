package kata.ratelimiter.webfluxapi.filter;

import kata.ratelimiter.webfluxapi.ratelimiter.RateLimiter;
import kata.ratelimiter.webfluxapi.ratelimiter.SlidingWindowLogRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiFilter implements WebFilter {

    @Value("${rate_limit_time_frame_seconds:60}")
    private int RATE_LIMIT_TIME_FRAME_SECONDS;
    @Value("${rate_limit_permits_in_a_frame:10}")
    private int RATE_LIMIT_PERMITS_IN_A_FRAME;

    Map<String, RateLimiter> rateLimitByKey = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var sourceIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        var uri = exchange.getRequest().getURI();
        var path = uri.getPath();

        var rateLimitKey = getKeyOfRateLimiter(sourceIP, path);
        rateLimitByKey.putIfAbsent(rateLimitKey,
                new SlidingWindowLogRateLimiter(RATE_LIMIT_TIME_FRAME_SECONDS, RATE_LIMIT_PERMITS_IN_A_FRAME));
        RateLimiter rateLimiter = rateLimitByKey.get(rateLimitKey);
        if (!rateLimiter.tryAcquire()) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    private String getKeyOfRateLimiter(String ip, String path) {
        return path + ip;
    }
}
