package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.InvalidProtocolBufferException;
import game.mode.*;
import game.redis.RedisService;
import game.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Random;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class HallClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int userId;

    private RedisService redisService;
    private MessageReceive messageReceive;
    private String ip;

    HallClient(RedisService redisService, MessageReceive messageReceive, String ip) {
        this.redisService = redisService;
        this.messageReceive = messageReceive;
        this.ip = ip;
    }

    synchronized void receive(GameBase.BaseConnection request) {
        try {
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
                    jsonObject.put("ip", ip);
                    ApiResponse<User> response = JSON.parseObject(HttpUtil.urlConnectionByRsa("http://127.0.0.1:9999/api/user/login_wechat", jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                    });
                    if ("SUCCESS".equals(response.getCode())) {
                        User user = response.getData();
                        HallTcpService.userClients.put(user.getUserId(), messageReceive);
                        userId = user.getUserId();
                        loginResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);
                        loginResponse.setID(user.getUserId());
                        loginResponse.setNickname(null == loginRequest.getNickname() ? "" : loginRequest.getUsername());
                        loginResponse.setHead(null == loginRequest.getHead() ? "" : loginRequest.getHead());
                        loginResponse.setLastLoginDate(new Date().getTime());
                        loginResponse.setLastLoginIp(ip);
                        loginResponse.setLastLoginAgent(loginRequest.getAgent());

                        //检测重连
                        if (redisService.exists("reconnect" + userId)) {
                            String reconnectString = redisService.getCache("reconnect" + userId);
                            String[] reconnectInfo = reconnectString.split(",");
                            if (redisService.exists("room" + reconnectInfo[1])) {
                                loginResponse.setInGame(true);
                                messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);
                                Hall.Reconnect.Builder reconnect = Hall.Reconnect.newBuilder();
                                reconnect.setRoomNo(reconnectInfo[1]);
                                if ("xingning_mahjong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.MAHJONG_XINGNING);
                                    reconnect.setIntoIp("192.168.3.99");
                                    reconnect.setPort(10001);
                                    messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                if ("ruijin_mahjong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.MAHJONG_RUIJIN);
                                    reconnect.setIntoIp("192.168.3.99");
                                    reconnect.setPort(10003);
                                    messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                if ("sangong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.SANGONG);
                                    reconnect.setIntoIp("192.168.3.99");
                                    reconnect.setPort(10004);
                                    messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                break;
                            }
                        }

                        loginResponse.setInGame(false);
                        messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);
                    } else {
                        messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN)
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
                                XingningMahjongRoom xingningMahjongRoom = new XingningMahjongRoom(xingningMahjongCreateRoomRequest.getBaseScore(),
                                        roomNo(), userId, xingningMahjongCreateRoomRequest.getGameTimes(), xingningMahjongCreateRoomRequest.getCount(),
                                        xingningMahjongCreateRoomRequest.getMaCount(), xingningMahjongCreateRoomRequest.getGhost(), xingningMahjongCreateRoomRequest.getGameRules());
                                redisService.addCache("room" + xingningMahjongRoom.getRoomNo(), JSON.toJSONString(xingningMahjongRoom));

                                redisService.addCache("reconnect" + userId, "xingning_mahjong," + xingningMahjongRoom.getRoomNo());

                                Hall.CreateRoomResponse createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                        .setRoomNo(xingningMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                        .setPort(10001).build();
                                messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                break;
                            case MAHJONG_RUIJIN:
                                Hall.RuijinMahjongCreateRoomRequest ruijinMahjongCreateRoomRequest = Hall.RuijinMahjongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                RuijinMahjongRoom ruijinMahjongRoom = new RuijinMahjongRoom(ruijinMahjongCreateRoomRequest.getBaseScore(), roomNo(), userId, ruijinMahjongCreateRoomRequest.getGameTimes(),
                                        ruijinMahjongCreateRoomRequest.getCount(), ruijinMahjongCreateRoomRequest.getDianpao(), ruijinMahjongCreateRoomRequest.getZhuangxian());
                                redisService.addCache("room" + ruijinMahjongRoom.getRoomNo(), JSON.toJSONString(ruijinMahjongRoom));

                                redisService.addCache("reconnect" + userId, "ruijin_mahjong," + ruijinMahjongRoom.getRoomNo());

                                createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                        .setRoomNo(ruijinMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                        .setPort(10003).build();
                                messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                break;
                            case RUN_QUICKLY:
                                Hall.RunQuicklyCreateRoomRequest runQuicklyCreateRoomRequest = Hall.RunQuicklyCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                RunQuicklyRoom runQuicklyRoom = new RunQuicklyRoom(runQuicklyCreateRoomRequest.getBaseScore(), roomNo(), runQuicklyCreateRoomRequest.getGameTimes(),
                                        runQuicklyCreateRoomRequest.getCount(), runQuicklyCreateRoomRequest.getGameRules());
                                redisService.addCache("room" + runQuicklyRoom.getRoomNo(), JSON.toJSONString(runQuicklyRoom));

                                createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                        .setRoomNo(runQuicklyRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                        .setPort(10002).build();
                                messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                break;
                            case SANGONG:
                                Hall.SanGongCreateRoomRequest sanGongCreateRoomRequest = Hall.SanGongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                SangongRoom sangongRoom = new SangongRoom(sanGongCreateRoomRequest.getBaseScore(), roomNo(), sanGongCreateRoomRequest.getGameTimes(), sanGongCreateRoomRequest.getBankerWay());
                                redisService.addCache("room" + sangongRoom.getRoomNo(), JSON.toJSONString(sangongRoom));

                                createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                        .setRoomNo(sangongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.3.99")
                                        .setPort(10004).build();
                                messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), userId);
                                break;
                        }

                        break;
                    }
                case REBACK:
                    Hall.RebackRequest rebackRequest = Hall.RebackRequest.parseFrom(request.getData());

                    if (redisService.exists("backkey" + rebackRequest.getBackKey())) {
                        userId = Integer.parseInt(redisService.getCache("backkey" + rebackRequest.getBackKey()));
                        HallTcpService.userClients.put(userId, messageReceive);
                    } else {
                        messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.REBACK)
                                .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.ERROR_KEY_INCORRECT).build().toByteString()).build(), 0);
                        break;
                    }
                    messageReceive.send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.REBACK)
                            .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.SUCCESS).build().toByteString()).build(), 0);
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
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

    void close() {
        synchronized (HallTcpService.userClients) {
            if (0 != userId && HallTcpService.userClients.containsKey(userId) && messageReceive == HallTcpService.userClients.get(userId)) {
                HallTcpService.userClients.remove(userId);
            }
        }

    }
}
