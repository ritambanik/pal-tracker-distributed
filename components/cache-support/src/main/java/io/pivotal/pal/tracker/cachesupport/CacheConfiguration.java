package io.pivotal.pal.tracker.cachesupport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

@Configuration
//@PropertySource(value = "classpath:application.properties")
public class CacheConfiguration {


    @Bean
    @Profile("!cloud")
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Map<String, Object>> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Map<String, Object>> template = new RedisTemplate<>();

        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public CacheService cacheService(RedisTemplate redisTemplate) {
        return new CacheService(redisTemplate);
    }
}
