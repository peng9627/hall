package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import game.mode.*;
import game.redis.RedisService;
import game.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class NoticeReceive implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket s;
    private final InputStream is;
    private final OutputStream os;

    private Boolean connect;
    private byte[] md5Key = "2704031cd4814eb2a82e47bd1d9042c6".getBytes();
    private RedisService redisService;

    NoticeReceive(Socket s, RedisService redisService) {

        this.s = s;
        connect = true;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
        } catch (EOFException e) {
            logger.info("socket.shutdown.message");
            close();
        } catch (IOException e) {
            logger.info("socket.connection.fail.message" + e.getMessage());
            close();
        }
        is = inputStream;
        os = outputStream;
        this.redisService = redisService;
    }

    public void close() {
        connect = false;
        try {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
            if (s != null) {
                s.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private int readInt(InputStream is) throws IOException {
        int ch1 = is.read();
        int ch2 = is.read();
        int ch3 = is.read();
        int ch4 = is.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return (ch1 << 24 | ((ch2 << 16) & 0xff) | ((ch3 << 8) & 0xff) | (ch4 & 0xFF));
    }

    @Override
    public void run() {
        try {
            while (connect) {
                int len = readInt(is);
                byte[] data = new byte[len];
                int l = is.read(data);
                SocketRequest socketRequest = JSON.parseObject(data, SocketRequest.class);
                switch (socketRequest.getNoticeType()) {
                    case 1:
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("userId", socketRequest.getUserId());
                        ApiResponse<User> userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa("http://127.0.0.1:9999/api/user/info", jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                        });
                        if (0 == userResponse.getCode() && HallTcpService.userClients.containsKey(socketRequest.getUserId())) {
                            Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(userResponse.getData().getMoney()).addCurrency(userResponse.getData().getIntegral()).build();
                            HallTcpService.userClients.get(socketRequest.getUserId()).send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), socketRequest.getUserId());
                        }
                        break;
                }
            }
        } catch (EOFException e) {
            logger.info("socket.shutdown.message");
            close();
        } catch (IOException e) {
            logger.info("socket.dirty.shutdown.message" + e.getMessage());
            e.printStackTrace();
            close();
        } catch (Exception e) {
            logger.info("socket.dirty.shutdown.message");
            e.printStackTrace();
            close();
        }
    }
}
