package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import game.constant.Constant;
import game.mode.*;
import game.redis.RedisService;
import game.utils.HttpUtil;
import game.utils.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Key;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class NoticeReceive implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket s;
    private final InputStream is;
    private final OutputStream os;
    private String requestPath;
    //post提交请求的正文的长度
    private int contentLength = 0;
    private RedisService redisService;
    private Key privateKey = null;

    NoticeReceive(Socket s, RedisService redisService) {

        this.s = s;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            privateKey = RSAUtils.getPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOaUJYNw5r2E5ar5lOa5ooCvvJCpFEYE1vUu1gCVcy265cJYxIZ1GIitGsSxMMME1FyOOb6yUW3QE4JmhHjG1w713j5IuSDyYgmcNos+c2xIzUZW7EwZON2pP2OX83ssGQe5bcl4iVnmqT/uJfckJTNfEZuzpoec/Ypb1G9et289AgMBAAECgYEAkxqC0FewLcrih3DRSV23SehUIepsz7r4tNWbnCW8pLkvKg1d2/ZKn6/oewIcfN7Q6Pen6Xx0LN3qBHCJJVCeFGv3FyJZ4wqzs3fiosZTX6m8heooEujeWknTGL3YYY7rIlpcvhvBbYL4NDl5OFUmvWRX8ahFHwPMKuTbvqSPJRUCQQD+v+fh6JKI807HWIwudWWU4Yja2gZUtgbVFu75ebV8pZaLEDtnWPxHiUEYyz4kCr27Ya6+bmpbfK0/QPu0YyjnAkEA57XerhPkcIfKEtwbQhaKulYeow4lNo12FNldLm2HwGHhWo2USOvhcu6zHWYGycaAIJJb0NnmsjkLlu4PV3CuOwJAKivYlhQrFdK5StTEt/glLcU8I4aOH73WWbYnL1NPkOfUiQbR3qTjdnApP5J9offJOtjL1ahvoN99yofWYyE7JwJASSMAzJV+z34s7FMJT4zp8PLp7LG0UUnJcb9CSDtOVA0RIpH5siKyIKLzal4f2mSLYLyRupRs2uhinhs6QHFSrQJBAJtzxXG0m2rJ3ew0EXMCoeFoyCxRZ/VppUz7MxfUpj9KBYFuqzkzdg3YOfxygNyGDHQzO/WEAW12F4brdHXjrPQ=");
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
        } catch (EOFException e) {
            logger.info("socket.shutdown.message");
            close();
        } catch (IOException e) {
            logger.info("socket.connection.fail.message" + e.getMessage());
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        is = inputStream;
        os = outputStream;
        this.redisService = redisService;
    }

    public void close() {
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

    @Override
    public void run() {
        try {
            DataInputStream reader = new DataInputStream((s.getInputStream()));
            String line = reader.readLine();
            String method = line.substring(0, 4).trim();
            this.requestPath = line.split(" ")[1];
            System.out.println(method);
            if ("POST".equalsIgnoreCase(method)) {
                this.doPost(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    //处理post请求
    private void doPost(DataInputStream reader) throws Exception {
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
            if ("".equals(line)) {
                break;
            } else if (line.contains("Content-Length")) {
                this.contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
            }
        }
        //用户发送的post数据正文
        byte[] buf;
        int size = 0;
        String param = null;
        if (this.contentLength != 0) {
            buf = new byte[this.contentLength];
            while (size < this.contentLength) {
                int c = reader.read();
                buf[size++] = (byte) c;

            }
            byte[] content = RSAUtils.decrypt(privateKey, URLDecoder.decode(new String(buf, "utf-8"), "utf-8").getBytes("utf-8"));
            if (null != content) {
                param = new String(content, "utf-8");
            }
        }
        SocketRequest socketRequest = JSON.parseObject(param, SocketRequest.class);
        ApiResponse apiResponse = new ApiResponse();
        switch (requestPath) {
            case "/1":
                synchronized (this) {
                    wait(1000);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", socketRequest.getUserId());
                ApiResponse<User> userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userInfoUrl, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                });
                if (0 == userResponse.getCode() && HallTcpService.userClients.containsKey(socketRequest.getUserId())) {
                    Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(userResponse.getData().getMoney())
                            .addCurrency(userResponse.getData().getIntegral()).addCurrency(userResponse.getData().getReward() == null ? 0 : (int) (userResponse.getData().getReward().doubleValue() * 100)).build();
                    HallTcpService.userClients.get(socketRequest.getUserId()).send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), socketRequest.getUserId());
                }
                apiResponse.setCode(0);
                break;
            case "/4":
                apiResponse.setCode(0);
                apiResponse.setData(HallTcpService.userClients.size());
                break;
        }

        returnData(apiResponse);
        os.flush();
        reader.close();
    }

    protected void returnData(ApiResponse apiResponse) {
        SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullBooleanAsFalse};
        int ss = SerializerFeature.config(JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteEnumUsingName, false);

        try {
            os.write("HTTP/1.1 200 OK\n\n".getBytes("utf-8"));
            byte[] bytes = RSAUtils.encrypt(privateKey, JSON.toJSONString(apiResponse, ss, features).getBytes("utf-8"));
            if (bytes != null) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
