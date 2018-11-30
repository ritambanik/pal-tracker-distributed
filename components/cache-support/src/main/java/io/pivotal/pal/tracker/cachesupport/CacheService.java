package io.pivotal.pal.tracker.cachesupport;

import org.springframework.data.redis.core.RedisTemplate;

public class CacheService {

    private final RedisTemplate redisTemplate;

    public CacheService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(long id, Object object, Class type) {
        redisTemplate.opsForHash().put(type.toString(), id, object);
    }

    public <T> T get(long id, Class<T> type) {
        return type.cast(redisTemplate.opsForHash().get(type.toString(), id));
    }

}
