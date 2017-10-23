package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import game.mode.*;
import game.mode.songjianghe.Room;
import game.redis.RedisService;
import game.utils.HttpUtil;
import game.utils.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

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
            logger.error(e.toString(), e);
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
        } catch (Exception e) {
            logger.error(e.toString(), e);
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
            case "/1"://更新货币
                synchronized (this) {
                    wait(1000);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", socketRequest.getUserId());
                ApiResponse<User> userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa("http://127.0.0.1:9999/api/user/info", jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                });
                if (0 == userResponse.getCode() && HallTcpService.userClients.containsKey(socketRequest.getUserId())) {
                    Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(userResponse.getData().getMoney()).addCurrency(userResponse.getData().getIntegral()).build();
                    HallTcpService.userClients.get(socketRequest.getUserId()).send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), socketRequest.getUserId());
                }
                apiResponse.setCode(0);
                break;
            case "/2"://更新单个代开房
                if (redisService.exists("room" + socketRequest.getContent())) {
                    Room room = JSON.parseObject(redisService.getCache("room" + socketRequest.getContent()), Room.class);
                    Hall.AgentRoomItem roomItem = Hall.AgentRoomItem.newBuilder().setCount(room.getCount())
                            .setCurrentCount(room.getSeats().size()).setGameRules(room.getGameRules()).setGameTimes(room.getGameTimes())
                            .setNormal(room.isNormal()).setSingleFan(room.isSingleFan()).setRoomNo(socketRequest.getContent()).build();
                    if (HallTcpService.userClients.containsKey(socketRequest.getUserId())) {
                        HallTcpService.userClients.get(socketRequest.getUserId()).send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.AGENT_ROOM_ITEM).setData(roomItem.toByteString()).build(), socketRequest.getUserId());
                    }
                }
                break;
            case "/3"://更新代开房列表
                while (!redisService.lock("lock_agent_rooms" + socketRequest.getUserId())) {
                }
                List<String> agentRooms;
                if (redisService.exists("agent_rooms" + socketRequest.getUserId())) {
                    agentRooms = JSON.parseArray(redisService.getCache("agent_rooms" + socketRequest.getUserId()), String.class);
                } else {
                    agentRooms = new ArrayList<>();
                }
                Hall.AgentRoomList.Builder agentRoomList = Hall.AgentRoomList.newBuilder();
                List<String> removeRooms = new ArrayList<>();
                for (String roomNo : agentRooms) {
                    if (redisService.exists("room" + roomNo)) {
                        Room room = JSON.parseObject(redisService.getCache("room" + roomNo), Room.class);
                        agentRoomList.addRoomItem(Hall.AgentRoomItem.newBuilder().setCount(room.getCount())
                                .setCurrentCount(room.getSeats().size()).setGameRules(room.getGameRules()).setGameTimes(room.getGameTimes())
                                .setNormal(room.isNormal()).setSingleFan(room.isSingleFan()).setRoomNo(roomNo));
                    } else {
                        removeRooms.add(roomNo);
                    }
                }
                agentRooms.removeAll(removeRooms);
                if (HallTcpService.userClients.containsKey(socketRequest.getUserId())) {
                    HallTcpService.userClients.get(socketRequest.getUserId()).send(GameBase.BaseConnection.newBuilder()
                            .setOperationType(GameBase.OperationType.AGENT_ROOM_LIST).setData(agentRoomList.build().toByteString()).build(), socketRequest.getUserId());
                }
                if (0 < agentRooms.size()) {
                    redisService.addCache("agent_rooms" + socketRequest.getUserId(), JSON.toJSONString(agentRooms));
                } else {
                    redisService.delete("agent_rooms" + socketRequest.getUserId());
                }
                redisService.unlock("lock_agent_rooms" + socketRequest.getUserId());
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
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }
}
