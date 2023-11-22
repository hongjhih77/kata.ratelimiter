package kata.ratelimiter.webfluxapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    public static final String REDISTEMPLATE_BEAN_NAME = "redisTemplateLettuce";
    public static final String LUA_TRY_ACQUIRE = "luaTryAcquire";

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 16379));
    }

    @Bean(REDISTEMPLATE_BEAN_NAME)
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(LUA_TRY_ACQUIRE)
    public RedisScript<String> luaTryAcquireScript() {
        Resource scriptSource = new ClassPathResource("SlidingWindowLogRateLimiter.lua");
        return RedisScript.of(scriptSource, String.class);
    }
}
