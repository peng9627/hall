package game.redis;

import redis.clients.jedis.ShardedJedisPool;

import java.util.List;

/**
 * Created by pengyi on 2016/3/21.
 */
public class RedisService {

    private final RedisClientTemplate redisClientTemplate;

    public RedisService(ShardedJedisPool shardedJedisPool) {
        this.redisClientTemplate = new RedisClientTemplate(shardedJedisPool);
    }

    public boolean exists(final String key) {
        return redisClientTemplate.exists(key);
    }

    public void addCache(final String key, final String value) {
        redisClientTemplate.set(key, value, 0);
    }

    public void addCache(final String key, final String value, int timeout) {
        redisClientTemplate.set(key, value, timeout);
    }

    public String getCache(final String key) {
        return redisClientTemplate.get(key);
    }

    public List<String> gets(String likeKey) {
        return redisClientTemplate.gets(likeKey);
    }

    public void delete(final String key) {
        if (redisClientTemplate.exists(key)) {
            redisClientTemplate.del(key);
        }
    }

    public boolean lock(String key) {
        return redisClientTemplate.lock(key, 6000);
    }

    public void lock(String key, long timeout) {
        redisClientTemplate.lock(key, timeout);
    }

    public void unlock(String key) {
        redisClientTemplate.unlock(key);
    }

}
