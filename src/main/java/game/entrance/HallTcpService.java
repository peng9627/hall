package game.entrance;

import game.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
    public final static Map<Integer, MessageReceive> userClients = new HashMap<>();

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private RedisService redisService;

    public HallTcpService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void run() {

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
                cachedThreadPool.execute(new MessageReceive(s, redisService));
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
