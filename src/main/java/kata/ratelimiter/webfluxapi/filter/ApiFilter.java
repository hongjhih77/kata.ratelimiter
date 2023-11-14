package kata.ratelimiter.webfluxapi.filter;

import kata.ratelimiter.webfluxapi.ratelimiter.RequestLimiter;
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

@Component
@Profile("ratelimit")
public class ApiFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    private final long rateLimitTimeFrameSeconds;
    private final int rateLimitPermitsInAFrame;
    private final RequestLimiter requestLimiter;

    @Autowired
    public ApiFilter(RequestLimiter requestLimiter,
                     @Value("${rate_limit_time_frame_seconds:60}") long timeFrameSeconds,
                     @Value("${rate_limit_permits_in_a_frame:10}") int permits) {
        this.rateLimitTimeFrameSeconds = timeFrameSeconds;
        this.rateLimitPermitsInAFrame = permits;
        this.requestLimiter = requestLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var sourceIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        var uri = exchange.getRequest().getURI();
        var path = uri.getPath();
        String rateLimitKey = getKeyOfRateLimiter(sourceIP, path);

        if (!requestLimiter.tryAcquire(rateLimitKey, rateLimitTimeFrameSeconds, rateLimitPermitsInAFrame)) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            logger.info("Request blocked: sourceIP : {}, path {}", sourceIP, path);
            return response.setComplete();
        }
        logger.info("Request permitted: sourceIP : {}, path {}", sourceIP, path);

        return chain.filter(exchange);
    }

    private String getKeyOfRateLimiter(String ip, String path) {
        return path + ";" + ip;
    }
}
