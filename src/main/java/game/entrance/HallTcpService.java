package game.entrance;

import game.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class HallTcpService implements Runnable {
    private ServerSocket serverSocket;
    private boolean started = false;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static Map<String, HallClient> userClients = new HashMap<>();

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public void run() {

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

        int port = 10000;
        try {
            serverSocket = new ServerSocket(port);
            started = true;
            logger.info("麻将tcp开启成功，端口[" + port + "]");
        } catch (IOException e) {
            logger.error("socket.open.fail.message");
            e.printStackTrace();
        }

        try {
            while (started) {
                Socket s = serverSocket.accept();
                cachedThreadPool.execute(new HallClient(s, redisService));
            }
        } catch (IOException e) {
            logger.error("socket.server.dirty.shutdown.message");
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
