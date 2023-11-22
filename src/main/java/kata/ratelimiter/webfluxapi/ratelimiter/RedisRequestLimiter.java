package kata.ratelimiter.webfluxapi.ratelimiter;


import jakarta.annotation.Resource;
import kata.ratelimiter.webfluxapi.configuration.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("ratelimit-redis")
public class RedisRequestLimiter implements RequestLimiter {
    private static final Logger logger = LoggerFactory.getLogger(RedisRequestLimiter.class);

    @Resource(name = RedisConfig.REDISTEMPLATE_BEAN_NAME)
    private RedisTemplate<String, String> redis;

    @Resource(name = RedisConfig.LUA_TRY_ACQUIRE)
    private RedisScript<String> luaTryAcquireScript;

    @Override
    public boolean tryAcquire(String rateLimitKey, long timeFrameSeconds, int permits) {
        String tryAcquireResult = redis.execute(luaTryAcquireScript, List.of(""), rateLimitKey,
                String.valueOf(timeFrameSeconds), String.valueOf(permits));
        var results = tryAcquireResult.split(",");
        //{true/false},{remain request available}
        logger.debug("rateLimitKey = {}, timeFrameSeconds = {}, permits = {}, redis rate limiter results = {}",
                rateLimitKey, timeFrameSeconds, permits, Arrays.toString(results));
        return "true".equals(results[0]);
    }
}
