package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.MessageLite;
import game.mode.*;
import game.redis.RedisService;
import game.utils.ByteUtils;
import game.utils.CoreStringUtils;
import game.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class HallClient implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket s;
    private int userId;
    private final InputStream is;
    private final OutputStream os;

    private RedisService redisService;
    private Boolean connect;
    private byte[] md5Key = "2704031cd4814eb2a82e47bd1d9042c6".getBytes();

    private List<User> users = new ArrayList<>();

    HallClient(Socket s, RedisService redisService) {

        this.s = s;
        connect = true;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
            this.redisService = redisService;
        } catch (EOFException e) {
            logger.info("socket.shutdown.message");
            close();
        } catch (IOException e) {
            logger.info("socket.connection.fail.message" + e.getMessage());
            close();
        }
        is = inputStream;
        os = outputStream;
    }

    public boolean send(MessageLite lite, int userId) {
        try {
            if (0 == userId) {
                String md5 = CoreStringUtils.md5(ByteUtils.addAll(md5Key, lite.toByteArray()), 32, false);
                int len = lite.toByteArray().length + md5.getBytes().length + 4;
                writeInt(os, len);
                writeString(os, md5);
                os.write(lite.toByteArray());
                logger.info("mahjong send:len=" + len);
                return true;
            }
            if (HallTcpService.userClients.containsKey(userId)) {
                synchronized (HallTcpService.userClients.get(userId).os) {
                    OutputStream os = HallTcpService.userClients.get(userId).os;
                    if (null != lite) {
                        String md5 = CoreStringUtils.md5(ByteUtils.addAll(md5Key, lite.toByteArray()), 32, false);
                        int len = lite.toByteArray().length + md5.getBytes().length + 4;
                        writeInt(os, len);
                        writeString(os, md5);
                        os.write(lite.toByteArray());
                        logger.info("mahjong send:len=" + len);
                    }
                }
                return true;
            }
        } catch (IOException e) {
            logger.info("socket.server.sendMessage.fail.message" + e.getMessage());
//            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
            if (0 != userId) {
//                exit();
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

    private String readString(InputStream is) throws IOException {
        int len = readInt(is);
        byte[] bytes = new byte[len];
        is.read(bytes);
        return new String(bytes);
    }

    private void writeInt(OutputStream stream, int value) throws IOException {
        stream.write((value >>> 24) & 0xFF);
        stream.write((value >>> 16) & 0xFF);
        stream.write((value >>> 8) & 0xFF);
        stream.write((value) & 0xFF);
    }

    private void writeString(OutputStream stream, String value) throws IOException {
        byte[] bytes = value.getBytes();
        writeInt(stream, bytes.length);
        stream.write(bytes);
    }


    @Override
    public void run() {
        try {
            while (connect) {
                int len = readInt(is);
                String md5 = readString(is);
                len -= md5.getBytes().length + 4;
                byte[] data = new byte[len];
                boolean check = true;
                if (0 != len) {
                    int l = is.read(data);
                    check = CoreStringUtils.md5(ByteUtils.addAll(md5Key, data), 32, false).equalsIgnoreCase(md5);
                }
                if (check) {
                    GameBase.BaseConnection request = GameBase.BaseConnection.parseFrom(data);
                    switch (request.getOperationType()) {
                        case LOGIN:
                            Hall.LoginRequest loginRequest = Hall.LoginRequest.parseFrom(request.getData());
                            Hall.LoginResponse.Builder loginResponse = Hall.LoginResponse.newBuilder();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("sex", loginRequest.getSex() ? "MAN" : "WOMAN");
                            jsonObject.put("weChatNo", loginRequest.getUsername());
                            jsonObject.put("area", "重庆市");
                            jsonObject.put("nickname", loginRequest.getUsername());
                            jsonObject.put("head", "");
                            jsonObject.put("agent", loginRequest.getAgent().name());
                            jsonObject.put("ip", s.getInetAddress().getHostAddress());
                            ApiResponse<User> response = JSON.parseObject(HttpUtil.urlConnectionByRsa("http://127.0.0.1:9999/api/user/login_wechat", jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                            });
                            if ("SUCCESS".equals(response.getCode())) {
                                User user = response.getData();
                                HallTcpService.userClients.put(user.getUserId(), this);
                                userId = user.getUserId();
                                loginResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);
                                loginResponse.setID(user.getUserId());
                                loginResponse.setNickname(null == loginRequest.getNickname() ? "" : loginRequest.getUsername());
                                loginResponse.setHead(null == loginRequest.getHead() ? "" : loginRequest.getHead());
                                loginResponse.setLastLoginDate(new Date().getTime());
                                loginResponse.setLastLoginIp(s.getInetAddress().getHostAddress());
                                loginResponse.setLastLoginAgent(loginRequest.getAgent());

                                //检测重连
                                if (redisService.exists("reconnect" + userId)) {
                                    String reconnectString = redisService.getCache("reconnect" + userId);
                                    String[] reconnectInfo = reconnectString.split(",");
                                    if (redisService.exists("room" + reconnectInfo[1])) {
                                        loginResponse.setInGame(true);
                                        send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);
                                        Hall.Reconnect.Builder reconnect = Hall.Reconnect.newBuilder();
                                        reconnect.setRoomNo(reconnectInfo[1]);
                                        if ("xingning_mahjong".equals(reconnectInfo[0])) {
                                            reconnect.setGameType(GameBase.GameType.MAHJONG_XINGNING);
                                            reconnect.setIntoIp("192.168.3.99");
                                            reconnect.setPort(10001);
                                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                        }
                                        if ("ruijin_mahjong".equals(reconnectInfo[0])) {
                                            reconnect.setGameType(GameBase.GameType.MAHJONG_RUIJIN);
                                            reconnect.setIntoIp("192.168.3.99");
                                            reconnect.setPort(10003);
                                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                        }
                                        if ("sangong".equals(reconnectInfo[0])) {
                                            reconnect.setGameType(GameBase.GameType.SANGONG);
                                            reconnect.setIntoIp("192.168.3.99");
                                            reconnect.setPort(10004);
                                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                        }
                                        break;
                                    }
                                }

                                loginResponse.setInGame(false);
                                send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);
                            } else {
                                send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN)
                                        .setData(loginResponse.setErrorCode(GameBase.ErrorCode.ERROR_UNKNOW).build().toByteString()).build(), 0);
                                break;
                            }

                            break;
                        case CREATE_ROOM:
                            if (0 != userId) {
                                Hall.BaseCreateRoomRequest createRoomRequest = Hall.BaseCreateRoomRequest.parseFrom(request.getData());
                                switch (createRoomRequest.getGameType()) {
                                    case MAHJONG_XINGNING:
                                        Hall.XingningMahjongCreateRoomRequest xingningMahjongCreateRoomRequest = Hall.XingningMahjongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                        Room room = new Room(xingningMahjongCreateRoomRequest.getBaseScore(), roomNo(), userId, xingningMahjongCreateRoomRequest.getGameTimes(),
                                                xingningMahjongCreateRoomRequest.getCount(), xingningMahjongCreateRoomRequest.getDianpao());
                                        redisService.addCache("room" + room.getRoomNo(), JSON.toJSONString(room));

                                        redisService.addCache("reconnect" + userId, "xingning_mahjong," + room.getRoomNo());

                                        Hall.CreateRoomResponse createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                                .setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                                .setPort(10001).build();
                                        send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                        break;
                                    case MAHJONG_RUIJIN:
                                        xingningMahjongCreateRoomRequest = Hall.XingningMahjongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                        room = new Room(xingningMahjongCreateRoomRequest.getBaseScore(), roomNo(), userId, xingningMahjongCreateRoomRequest.getGameTimes(),
                                                xingningMahjongCreateRoomRequest.getCount(), xingningMahjongCreateRoomRequest.getDianpao());
                                        redisService.addCache("room" + room.getRoomNo(), JSON.toJSONString(room));

                                        redisService.addCache("reconnect" + userId, "ruijin_mahjong," + room.getRoomNo());

                                        createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                                .setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                                .setPort(10003).build();
                                        send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                        break;
                                    case RUN_QUICKLY:
                                        Hall.RunQuicklyCreateRoomRequest runQuicklyCreateRoomRequest = Hall.RunQuicklyCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                        RunQuicklyRoom runQuicklyRoom = new RunQuicklyRoom(runQuicklyCreateRoomRequest.getBaseScore(), roomNo(), runQuicklyCreateRoomRequest.getGameTimes(), runQuicklyCreateRoomRequest.getCount());
                                        redisService.addCache("room" + runQuicklyRoom.getRoomNo(), JSON.toJSONString(runQuicklyRoom));

                                        createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                                .setRoomNo(runQuicklyRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                                .setPort(10002).build();
                                        send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                        break;
                                    case SANGONG:
                                        Hall.SanGongCreateRoomRequest sanGongCreateRoomRequest = Hall.SanGongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                        SangongRoom sangongRoom = new SangongRoom(sanGongCreateRoomRequest.getBaseScore(), roomNo(), sanGongCreateRoomRequest.getGameTimes(), sanGongCreateRoomRequest.getCount());
                                        redisService.addCache("room" + sangongRoom.getRoomNo(), JSON.toJSONString(sangongRoom));

                                        createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                                .setRoomNo(sangongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                                .setPort(10004).build();
                                        send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                        break;
                                }

                                break;
                            }
                        case REBACK:
                            Hall.RebackRequest rebackRequest = Hall.RebackRequest.parseFrom(request.getData());

                            if (redisService.exists("backkey" + rebackRequest.getBackKey())) {
                                userId = Integer.parseInt(redisService.getCache("backkey" + rebackRequest.getBackKey()));
                                HallTcpService.userClients.put(userId, this);
                            } else {
                                send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.REBACK)
                                        .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.ERROR_KEY_INCORRECT).build().toByteString()).build(), 0);
                                break;
                            }
                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.REBACK)
                                    .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.SUCCESS).build().toByteString()).build(), 0);
                            break;
                    }
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

    /**
     * 生成随机桌号
     *
     * @return 桌号
     */
    private String roomNo() {
        String roomNo = (new Random().nextInt(899999) + 100001) + "";
        if (redisService.exists(roomNo + "")) {
            roomNo = roomNo();
        }
        return roomNo;
    }
}
