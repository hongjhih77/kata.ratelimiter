package kata.ratelimiter.webfluxapi.filter;

import kata.ratelimiter.webfluxapi.ratelimiter.RateLimiter;
import kata.ratelimiter.webfluxapi.ratelimiter.SlidingWindowLogRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("ratelimit")
public class ApiFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    private final long rateLimitTimeFrameSeconds;
    private final int rateLimitPermitsInAFrame;

    private static final String HEADER_X_RATELIMIT_LIMIT = "X-Ratelimit-Limit";
    Map<String, RateLimiter> rateLimitByKey = new ConcurrentHashMap<>();

    @Autowired
    public ApiFilter(@Value("${rate_limit_time_frame_seconds:60}") long timeFrameSeconds,
                     @Value("${rate_limit_permits_in_a_frame:10}") int permits) {
        this.rateLimitTimeFrameSeconds = timeFrameSeconds;
        this.rateLimitPermitsInAFrame = permits;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var sourceIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        var uri = exchange.getRequest().getURI();
        var path = uri.getPath();

        var rateLimitKey = getKeyOfRateLimiter(sourceIP, path);
        rateLimitByKey.putIfAbsent(rateLimitKey,
                new SlidingWindowLogRateLimiter(rateLimitTimeFrameSeconds, rateLimitPermitsInAFrame));

        RateLimiter rateLimiter = rateLimitByKey.get(rateLimitKey);
        if (!rateLimiter.tryAcquire()) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            logger.info("Request blocked: key : {}, acquiring {}", rateLimitKey, rateLimiter.getAcquired());
            return response.setComplete();
        }
        logger.info("Request permitted: key : {}, acquiring {}", rateLimitKey, rateLimiter.getAcquired());

        return chain.filter(exchange);
    }

    private String getKeyOfRateLimiter(String ip, String path) {
        return path + ";" + ip;
    }
}
