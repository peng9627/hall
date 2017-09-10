package game.entrance;

import game.redis.RedisService;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class StartGame {
    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(50000);
        jedisPoolConfig.setMaxIdle(20000);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);

        List<JedisShardInfo> shards = new ArrayList<>();
        shards.add(new JedisShardInfo("127.0.0.1", 6379, 300));
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(jedisPoolConfig, shards);
        RedisService redisService = new RedisService(shardedJedisPool);
        new Thread(new HallTcpService(redisService)).start();
        new Thread(new HallNoticeService(redisService)).start();
    }
}
