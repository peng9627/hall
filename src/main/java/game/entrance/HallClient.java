package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.InvalidProtocolBufferException;
import game.constant.Constant;
import game.mode.*;
import game.mode.ruijin.RuijinMahjongRoom;
import game.mode.runquickly.RunQuicklyRoom;
import game.mode.sangong.SangongRoom;
import game.mode.xingning.OperationHistory;
import game.mode.xingning.Record;
import game.mode.xingning.SeatRecord;
import game.mode.xingning.XingningMahjongRoom;
import game.redis.RedisService;
import game.utils.HttpUtil;
import game.utils.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class HallClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public int userId;

    private RedisService redisService;
    private MessageReceive messageReceive;
    private String ip;
    private GameBase.BaseConnection.Builder response;
    private JSONObject jsonObject;
    private int version;

    HallClient(RedisService redisService, MessageReceive messageReceive, String ip) {
        this.redisService = redisService;
        this.messageReceive = messageReceive;
        this.ip = ip;
        this.response = GameBase.BaseConnection.newBuilder();
        this.jsonObject = new JSONObject();
    }

    synchronized void receive(GameBase.BaseConnection request) {
        try {
            logger.info("接收" + userId + request.getOperationType().toString());
            switch (request.getOperationType()) {
                case HEARTBEAT:
                    messageReceive.send(response.setOperationType(GameBase.OperationType.HEARTBEAT).clearData().build(), userId);
                    break;
                case LOGIN:
                    Hall.LoginRequest loginRequest = Hall.LoginRequest.parseFrom(request.getData());
                    Hall.LoginResponse.Builder loginResponse = Hall.LoginResponse.newBuilder();
                    jsonObject.clear();
                    jsonObject.put("sex", loginRequest.getSex() ? "MAN" : "WOMAN");
                    jsonObject.put("weChatNo", loginRequest.getUsername());
                    jsonObject.put("area", "");
                    jsonObject.put("nickname", loginRequest.getNickname());
                    jsonObject.put("head", loginRequest.getHead());
                    jsonObject.put("agent", loginRequest.getAgent().name());
                    jsonObject.put("ip", ip);
                    ApiResponse<User> response = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.wechatLogin, jsonObject.toJSONString()),
                            new TypeReference<ApiResponse<User>>() {
                            });
                    if (0 == response.getCode()) {
                        User user = response.getData();
                        if (HallTcpService.userClients.containsKey(userId) && HallTcpService.userClients.get(userId) != messageReceive) {
                            HallTcpService.userClients.get(userId).close();
                        }
                        synchronized (this) {
                            try {
                                wait(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        HallTcpService.userClients.put(user.getUserId(), messageReceive);
                        userId = user.getUserId();
                        loginResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);

                        //检测重连
                        if (redisService.exists("reconnect" + userId)) {
                            String reconnectString = redisService.getCache("reconnect" + userId);
                            String[] reconnectInfo = reconnectString.split(",");
                            if (redisService.exists("room" + reconnectInfo[1])) {
                                loginResponse.setInGame(true);
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);

                                Hall.UserInfoResponse.Builder userInfoResponse = Hall.UserInfoResponse.newBuilder();
                                userInfoResponse.setID(user.getUserId());
                                userInfoResponse.setNickname(null == loginRequest.getNickname() ? "" : loginRequest.getNickname());
                                userInfoResponse.setHead(null == loginRequest.getHead() ? "" : loginRequest.getHead());
                                userInfoResponse.setLastLoginDate(new Date().getTime());
                                userInfoResponse.setLastLoginIp(ip);
                                userInfoResponse.setLastLoginAgent(loginRequest.getAgent());
                                userInfoResponse.setGameCount(user.getGameCount());
                                userInfoResponse.setSex(user.getSex().equals("SEX"));
                                userInfoResponse.setTodayGameCount(user.getTodayGameCount());
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.USER_INFO).setData(userInfoResponse.build().toByteString()).build(), userId);

                                Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(user.getMoney()).addCurrency(user.getIntegral()).build();
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), userId);

                                Hall.Reconnect.Builder reconnect = Hall.Reconnect.newBuilder();
                                reconnect.setRoomNo(reconnectInfo[1]);
                                if ("xingning_mahjong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.MAHJONG_XINGNING);
                                    reconnect.setIntoIp(Constant.gameServerIp);
                                    reconnect.setPort(10001);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                if ("run_quickly".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.RUN_QUICKLY);
                                    reconnect.setIntoIp(Constant.gameServerIp);
                                    reconnect.setPort(10002);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                if ("ruijin_mahjong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.MAHJONG_RUIJIN);
                                    reconnect.setIntoIp(Constant.gameServerIp);
                                    reconnect.setPort(10003);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                if ("sangong".equals(reconnectInfo[0])) {
                                    reconnect.setGameType(GameBase.GameType.SANGONG);
                                    reconnect.setIntoIp(Constant.gameServerIp);
                                    reconnect.setPort(10004);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
                                }
                                break;
                            }
                        }

                        loginResponse.setInGame(false);
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), userId);

                        Hall.UserInfoResponse.Builder userInfoResponse = Hall.UserInfoResponse.newBuilder();
                        userInfoResponse.setID(user.getUserId());
                        userInfoResponse.setNickname(null == loginRequest.getNickname() ? "" : loginRequest.getNickname());
                        userInfoResponse.setHead(null == loginRequest.getHead() ? "" : loginRequest.getHead());
                        userInfoResponse.setLastLoginDate(new Date().getTime());
                        userInfoResponse.setLastLoginIp(ip);
                        userInfoResponse.setLastLoginAgent(loginRequest.getAgent());
                        userInfoResponse.setGameCount(user.getGameCount());
                        userInfoResponse.setSex(user.getSex().equals("SEX"));
                        userInfoResponse.setTodayGameCount(user.getTodayGameCount());
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.USER_INFO).setData(userInfoResponse.build().toByteString()).build(), userId);

                        Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(user.getMoney()).addCurrency(user.getIntegral()).build();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), userId);

                        GameBase.RecordResponse recordResponse = gameRecord();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECORD).setData(recordResponse.toByteString()).build(), userId);

                        Hall.TaskResponse taskResponse = Hall.TaskResponse.newBuilder().setCount(100).setName("每日对战100局")
                                .setReward(100).setTodayGameCount(user.getTodayGameCount()).build();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.TASK).setData(taskResponse.toByteString()).build(), userId);
                    } else {
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.LOGIN)
                                .setData(loginResponse.setErrorCode(GameBase.ErrorCode.ERROR_UNKNOW).build().toByteString()).build(), 0);
                        break;
                    }

                    break;
                case CREATE_ROOM:
                    jsonObject.clear();
                    jsonObject.put("userId", userId);
                    ApiResponse<User> userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userInfoUrl, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                    });
                    if (0 == userResponse.getCode()) {
                        Hall.BaseCreateRoomRequest createRoomRequest = Hall.BaseCreateRoomRequest.parseFrom(request.getData());
                        jsonObject.clear();
                        jsonObject.put("userId", userId);
                        jsonObject.put("flowType", 2);
                        boolean moneyEnough = true;
                        switch (createRoomRequest.getGameType()) {
                            case MAHJONG_XINGNING:
                                Hall.XingningMahjongCreateRoomRequest xingningMahjongCreateRoomRequest = Hall.XingningMahjongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                XingningMahjongRoom xingningMahjongRoom = new XingningMahjongRoom(xingningMahjongCreateRoomRequest.getBaseScore(),
                                        roomNo(), userId, xingningMahjongCreateRoomRequest.getGameTimes(), xingningMahjongCreateRoomRequest.getCount(),
                                        xingningMahjongCreateRoomRequest.getMaCount(), xingningMahjongCreateRoomRequest.getGhost(), xingningMahjongCreateRoomRequest.getGameRules());
                                Hall.RoomResponse.Builder createRoomResponse = Hall.RoomResponse.newBuilder();
                                if (0 == xingningMahjongRoom.getCount()) {
                                    createRoomResponse.setRoomNo(xingningMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW).build();
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                jsonObject.put("description", "开房间" + xingningMahjongRoom.getRoomNo());
                                if (8 == xingningMahjongCreateRoomRequest.getGameTimes()) {
                                    jsonObject.put("money", 1);
                                } else {
                                    jsonObject.put("money", 2);
                                }
                                if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
                                    moneyEnough = false;
                                    createRoomResponse.setError(GameBase.ErrorCode.MONEY_NOT_ENOUGH);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                redisService.addCache("room" + xingningMahjongRoom.getRoomNo(), JSON.toJSONString(xingningMahjongRoom));
                                redisService.addCache("reconnect" + userId, "xingning_mahjong," + xingningMahjongRoom.getRoomNo());
                                redisService.addCache("room_type" + xingningMahjongRoom.getRoomNo(), "xingning_mahjong");

                                createRoomResponse.setRoomNo(xingningMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                        .setIntoIp(Constant.gameServerIp).setPort(10001).build();
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                break;
                            case MAHJONG_RUIJIN:
                                Hall.RuijinMahjongCreateRoomRequest ruijinMahjongCreateRoomRequest = Hall.RuijinMahjongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                RuijinMahjongRoom ruijinMahjongRoom = new RuijinMahjongRoom(ruijinMahjongCreateRoomRequest.getBaseScore(), roomNo(), userId, ruijinMahjongCreateRoomRequest.getGameTimes(),
                                        ruijinMahjongCreateRoomRequest.getCount(), ruijinMahjongCreateRoomRequest.getDianpao(), ruijinMahjongCreateRoomRequest.getZhuangxian());
                                createRoomResponse = Hall.RoomResponse.newBuilder();
                                if (0 == ruijinMahjongRoom.getCount()) {
                                    createRoomResponse.setRoomNo(ruijinMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW).build();
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                jsonObject.put("description", "开房间" + ruijinMahjongRoom.getRoomNo());
                                switch (ruijinMahjongRoom.getGameTimes()) {
                                    case 4:
                                        jsonObject.put("money", 1);
                                        break;
                                    case 8:
                                        jsonObject.put("money", 2);
                                        break;
                                    case 16:
                                        jsonObject.put("money", 3);
                                        break;
                                }
                                if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
                                    moneyEnough = false;
                                    createRoomResponse.setError(GameBase.ErrorCode.MONEY_NOT_ENOUGH);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                redisService.addCache("room" + ruijinMahjongRoom.getRoomNo(), JSON.toJSONString(ruijinMahjongRoom));
                                redisService.addCache("reconnect" + userId, "ruijin_mahjong," + ruijinMahjongRoom.getRoomNo());
                                redisService.addCache("room_type" + ruijinMahjongRoom.getRoomNo(), "ruijin_mahjong");

                                createRoomResponse.setRoomNo(ruijinMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                        .setIntoIp(Constant.gameServerIp).setPort(10003).build();
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                break;
                            case RUN_QUICKLY:
                                Hall.RunQuicklyCreateRoomRequest runQuicklyCreateRoomRequest = Hall.RunQuicklyCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                RunQuicklyRoom runQuicklyRoom = new RunQuicklyRoom(runQuicklyCreateRoomRequest.getBaseScore(), roomNo(), runQuicklyCreateRoomRequest.getGameTimes(),
                                        runQuicklyCreateRoomRequest.getCount(), runQuicklyCreateRoomRequest.getGameRules(), userId);
                                jsonObject.put("description", "开房间" + runQuicklyRoom.getRoomNo());
                                createRoomResponse = Hall.RoomResponse.newBuilder();
                                if (0 == runQuicklyRoom.getCount()) {
                                    createRoomResponse.setRoomNo(runQuicklyRoom.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW).build();
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                switch (runQuicklyRoom.getGameTimes()) {
                                    case 4:
                                        jsonObject.put("money", 1);
                                        break;
                                    case 8:
                                        jsonObject.put("money", 2);
                                        break;
                                    case 16:
                                        jsonObject.put("money", 3);
                                        break;
                                }
                                createRoomResponse = Hall.RoomResponse.newBuilder();
                                if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
                                    moneyEnough = false;
                                    createRoomResponse.setError(GameBase.ErrorCode.MONEY_NOT_ENOUGH);
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                redisService.addCache("room" + runQuicklyRoom.getRoomNo(), JSON.toJSONString(runQuicklyRoom));
                                redisService.addCache("reconnect" + userId, "run_quickly," + runQuicklyRoom.getRoomNo());
                                redisService.addCache("room_type" + runQuicklyRoom.getRoomNo(), "run_quickly");

                                createRoomResponse.setRoomNo(runQuicklyRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                        .setIntoIp(Constant.gameServerIp).setPort(10002).build();
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                break;
                            case SANGONG:
                                Hall.SanGongCreateRoomRequest sanGongCreateRoomRequest = Hall.SanGongCreateRoomRequest.parseFrom(createRoomRequest.getData());
                                SangongRoom sangongRoom = new SangongRoom(sanGongCreateRoomRequest.getBaseScore(), roomNo(),
                                        sanGongCreateRoomRequest.getGameTimes(), sanGongCreateRoomRequest.getBankerWay(),
                                        userId, sanGongCreateRoomRequest.getPayType(), sanGongCreateRoomRequest.getCount());
                                createRoomResponse = Hall.RoomResponse.newBuilder();
                                if (0 == sangongRoom.getCount()) {
                                    createRoomResponse.setRoomNo(sangongRoom.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW).build();
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                    break;
                                }
                                if (1 == sanGongCreateRoomRequest.getPayType()) {
                                    jsonObject.put("description", "开房间" + sangongRoom.getRoomNo());
                                    switch (sangongRoom.getGameTimes()) {
                                        case 10:
                                            jsonObject.put("money", 3);
                                            break;
                                        case 20:
                                            jsonObject.put("money", 6);
                                            break;
                                    }
                                    if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
                                        moneyEnough = false;
                                        createRoomResponse.setError(GameBase.ErrorCode.MONEY_NOT_ENOUGH);
                                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                        break;
                                    }
                                }
                                redisService.addCache("room" + sangongRoom.getRoomNo(), JSON.toJSONString(sangongRoom));
                                redisService.addCache("reconnect" + userId, "sangong," + sangongRoom.getRoomNo());
                                redisService.addCache("room_type" + sangongRoom.getRoomNo(), "sangong");

                                createRoomResponse.setRoomNo(sangongRoom.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                        .setIntoIp(Constant.gameServerIp).setPort(10004).build();
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                                break;
                        }
                        if (moneyEnough && jsonObject.containsKey("money")) {
                            ApiResponse moneyDetail = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.moneyDetailedCreate, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                            });
                            if (0 != moneyDetail.getCode()) {
                                LoggerFactory.getLogger(this.getClass()).error(Constant.apiUrl + Constant.moneyDetailedCreate + "?" + jsonObject.toJSONString());
                            }
                        }
                    }
                    break;
                case ADD_ROOM:
                    Hall.AddToRoomRequest addToRoomRequest = Hall.AddToRoomRequest.parseFrom(request.getData());
                    Hall.RoomResponse.Builder createRoomResponse = Hall.RoomResponse.newBuilder();
                    if (redisService.exists("room" + addToRoomRequest.getRoomNo())) {
                        switch (redisService.getCache("room_type" + addToRoomRequest.getRoomNo())) {
                            case "xingning_mahjong":
                                XingningMahjongRoom xingningMahjongRoom = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), XingningMahjongRoom.class);

                                if (0 != xingningMahjongRoom.getGameStatus().compareTo(GameStatus.WAITING)) {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                                } else {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                            .setIntoIp(Constant.gameServerIp).setPort(10001);
                                }
                                break;
                            case "ruijin_mahjong":
                                RuijinMahjongRoom ruijinMahjongRoom = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), RuijinMahjongRoom.class);
                                if (0 != ruijinMahjongRoom.getGameStatus().compareTo(GameStatus.WAITING)) {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                                } else {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                            .setIntoIp(Constant.gameServerIp).setPort(10003);
                                }
                                break;
                            case "run_quickly":
                                RunQuicklyRoom runQuicklyRoom = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), RunQuicklyRoom.class);
                                if (0 != runQuicklyRoom.getGameStatus().compareTo(GameStatus.WAITING)) {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                                } else {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                            .setIntoIp(Constant.gameServerIp).setPort(10002);
                                }
                                break;
                            case "sangong":
                                SangongRoom sangongRoom = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), SangongRoom.class);
                                if (2 == sangongRoom.getPayType()) {
                                    jsonObject.clear();
                                    jsonObject.put("userId", userId);
                                    userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userInfoUrl, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                                    });
                                    if (0 == userResponse.getCode()) {
                                        if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
                                            createRoomResponse.setError(GameBase.ErrorCode.MONEY_NOT_ENOUGH);
                                            break;
                                        }
                                    }

                                }
                                if (0 != sangongRoom.getGameStatus().compareTo(GameStatus.WAITING)) {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                                } else {
                                    createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                            .setIntoIp(Constant.gameServerIp).setPort(10004);
                                }
                                break;
                        }
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.ADD_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                    } else {
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.ADD_ROOM).setData(Hall.RoomResponse.newBuilder()
                                .setError(GameBase.ErrorCode.ROOM_NOT_EXIST).build().toByteString()).build(), userId);
                    }

                    break;
                case REBACK:
                    Hall.RebackRequest rebackRequest = Hall.RebackRequest.parseFrom(request.getData());

                    if (redisService.exists("backkey" + rebackRequest.getBackKey())) {
                        userId = Integer.parseInt(redisService.getCache("backkey" + rebackRequest.getBackKey()));
                        HallTcpService.userClients.put(userId, messageReceive);
                    } else {
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.REBACK)
                                .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.ERROR_KEY_INCORRECT).build().toByteString()).build(), 0);
                        break;
                    }
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.REBACK)
                            .setData(Hall.RebackResponse.newBuilder().setError(GameBase.ErrorCode.SUCCESS).build().toByteString()).build(), 0);

                    jsonObject.clear();
                    jsonObject.put("userId", userId);
                    userResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userInfoUrl, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                    });
                    if (0 == userResponse.getCode()) {
                        User user = userResponse.getData();
                        Hall.UserInfoResponse.Builder userInfoResponse = Hall.UserInfoResponse.newBuilder();
                        userInfoResponse.setID(user.getUserId());
                        userInfoResponse.setNickname(null == user.getNickname() ? "" : user.getNickname());
                        userInfoResponse.setHead(null == user.getHead() ? "" : user.getHead());
                        userInfoResponse.setLastLoginDate(new Date().getTime());
                        userInfoResponse.setLastLoginIp(ip);
                        userInfoResponse.setLastLoginAgent(Hall.Agent.forNumber(Integer.valueOf(user.getAgent())));
                        userInfoResponse.setGameCount(user.getGameCount());
                        userInfoResponse.setSex(user.getSex().equals("SEX"));
                        userInfoResponse.setTodayGameCount(user.getTodayGameCount());
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.USER_INFO).setData(userInfoResponse.build().toByteString()).build(), userId);

                        Hall.CurrencyResponse currencyResponse = Hall.CurrencyResponse.newBuilder().addCurrency(user.getMoney()).addCurrency(user.getIntegral()).build();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.CURRENCY).setData(currencyResponse.toByteString()).build(), userId);

                        GameBase.RecordResponse recordResponse = gameRecord();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECORD).setData(recordResponse.toByteString()).build(), userId);

                        Hall.TaskResponse taskResponse = Hall.TaskResponse.newBuilder().setCount(100).setName("每日对战100局")
                                .setReward(100).setTodayGameCount(user.getTodayGameCount()).build();
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.TASK).setData(taskResponse.toByteString()).build(), userId);
                    }

                    break;

                case RECORD_DETAILS:
                    GameBase.RecordDetailsRequest recordDetailsRequest = GameBase.RecordDetailsRequest.parseFrom(request.getData());
                    GameBase.RecordDetailsResponse recordDetailsResponse = recordDetails(recordDetailsRequest.getRecordId());
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECORD_DETAILS).setData(recordDetailsResponse.toByteString()).build(), userId);
                    break;

                case REPLAY:
                    GameBase.ReplayRequest replayRequest = GameBase.ReplayRequest.parseFrom(request.getData());
                    GameBase.ReplayResponse replayResponse = replay(replayRequest.getRecordId(), replayRequest.getRound());
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.REPLAY).setData(replayResponse.toByteString()).build(), userId);
                    break;
                case SHARE_SUCCESS:
                    jsonObject.clear();
                    jsonObject.put("userId", userId);
                    ApiResponse apiResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.share, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                    });
                    this.response.setOperationType(GameBase.OperationType.SHARE_SUCCESS);
                    if (0 == apiResponse.getCode()) {
                        this.response.setData(Hall.ShareSuccessResponse.newBuilder().setError(GameBase.ErrorCode.SUCCESS).build().toByteString());
                    } else if (9 == apiResponse.getCode()) {
                        this.response.setData(Hall.ShareSuccessResponse.newBuilder().setError(GameBase.ErrorCode.ERROR_SHARED).build().toByteString());
                    } else {
                        this.response.setData(Hall.ShareSuccessResponse.newBuilder().setError(GameBase.ErrorCode.ERROR_UNKNOW).build().toByteString());
                    }
                    break;
                case COMPETITION_LIST:
                    Hall.RegistrationListRequest registrationListRequest = Hall.RegistrationListRequest.parseFrom(request.getData());
                    jsonObject.clear();
                    jsonObject.put("gameType", registrationListRequest.getGameType().getNumber());
                    ApiResponse<List<Arena>> arenasResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.arenaListUrl, jsonObject.toJSONString()),
                            new TypeReference<ApiResponse<List<Arena>>>() {
                            });
                    Hall.RegistrationListResponse.Builder registrationListResponse = Hall.RegistrationListResponse.newBuilder();
                    if (0 == arenasResponse.getCode()) {
                        for (Arena arena : arenasResponse.getData()) {
                            int size = 0;
                            if (redisService.exists("registration_population" + arena.getId())) {
                                redisService.lock("lock_registration_population" + arena.getId());
                                List<Integer> people = JSON.parseArray(redisService.getCache("registration_population" + arena.getId()), Integer.class);
                                size = people.size();
                            }

                            registrationListResponse.addRegistration(Hall.Registration.newBuilder().setId(arena.getId())
                                    .setArenaType(arena.getArenaType()).setCount(arena.getCount()).setEntryFee(arena.getEntryFee())
                                    .setName(arena.getName()).setReward(arena.getReward()).setCurrentCount(size));
                        }
                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.COMPETITION_LIST).setData(registrationListResponse.build().toByteString()).build(), userId);
                    }
                    break;
                case REGISTRATION:
                    Hall.RegistrationRequest registrationRequest = Hall.RegistrationRequest.parseFrom(request.getData());
                    ApiResponse<Arena> arenaResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.arenaInfoUrl + registrationRequest.getId(), null),
                            new TypeReference<ApiResponse<Arena>>() {
                            });
                    Hall.RegistrationResponse.Builder registrationResponse = Hall.RegistrationResponse.newBuilder();
                    if (0 == arenaResponse.getCode()) {
                        if (redisService.exists("registration_population" + registrationRequest.getId())) {
                            redisService.lock("lock_registration_population" + registrationRequest.getId());

                            List<Integer> people = JSON.parseArray(redisService.getCache("registration_population" + registrationRequest.getId()), Integer.class);
                            if (people.contains(userId)) {
                                registrationResponse.setError(GameBase.ErrorCode.AREADY_REGISTRATION);
                            } else {
                                registrationResponse.setError(GameBase.ErrorCode.SUCCESS);
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.REGISTRATION).setData(registrationResponse.build().toByteString()).build(), userId);
                                people.add(userId);
                                Arena arena = arenaResponse.getData();


                                //人数满，开始比赛
                                if (people.size() == arena.getCount()) {

                                    List<MatchUser> matchUsers = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (Integer integer : people) {
                                        stringBuilder.append(",").append(integer);
                                        MatchUser matchUser = new MatchUser();
                                        matchUser.setUserId(integer);
                                        matchUser.setScore(1000);
                                        matchUsers.add(matchUser);
                                    }
                                    jsonObject.clear();
                                    jsonObject.put("userIds", stringBuilder.toString().substring(1));
                                    ApiResponse<List<User>> usersResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userListUrl, jsonObject.toJSONString()),
                                            new TypeReference<ApiResponse<List<User>>>() {
                                            });
                                    if (0 == usersResponse.getCode()) {
                                        List<User> userList = usersResponse.getData();

                                        int matchNo = 0;
                                        while (true) {
                                            int temp = (new Random().nextInt(899999) + 100001);
                                            if (!redisService.exists("matchNo" + matchNo)) {
                                                matchNo = temp;
                                                break;
                                            }
                                        }

                                        List<Integer> roomNos = new ArrayList<>();
                                        //三公优先6人一桌，其它游戏4人
                                        while (userList.size() > 0) {
                                            switch (arena.getGameType()) {
                                                case XINGNING_MAHJONG:
                                                    XingningMahjongRoom xingningMahjongRoom = new XingningMahjongRoom(1, roomNo(), userList.get(0).getUserId(), 1, 4, 0, 1, 16382);
                                                    Hall.RoomResponse roomResponse = Hall.RoomResponse.newBuilder().setIntoIp(Constant.gameServerIp)
                                                            .setPort(10001).setRoomNo(String.valueOf(xingningMahjongRoom.getRoomNo())).build();
                                                    if (userList.size() >= 4) {
                                                        xingningMahjongRoom.addSeats(userList.subList(0, 4));
                                                        for (int i = 0; i < 4; i++) {
                                                            redisService.addCache("reconnect" + userList.get(i).getUserId(), "xingning_mahjong," + xingningMahjongRoom.getRoomNo());
                                                            if (HallTcpService.userClients.containsKey(userList.get(i).getUserId())) {
                                                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.COMPETITION_START).setData(roomResponse.toByteString()).build(), userList.get(i).getUserId());
                                                            }
                                                        }
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                    }
                                                    roomNos.add(Integer.parseInt(xingningMahjongRoom.getRoomNo()));
                                                    redisService.addCache("room" + xingningMahjongRoom.getRoomNo(), JSON.toJSONString(xingningMahjongRoom));

                                                    redisService.addCache("room_type" + xingningMahjongRoom.getRoomNo(), "xingning_mahjong");
                                                    redisService.addCache("room_match" + xingningMahjongRoom.getRoomNo(), String.valueOf(matchNo));
                                                    break;
                                                case RUN_QUICKLY:
                                                    RunQuicklyRoom runQuicklyRoom = new RunQuicklyRoom(1, roomNo(), 1, 4, 16382, userId);
                                                    roomResponse = Hall.RoomResponse.newBuilder().setIntoIp(Constant.gameServerIp)
                                                            .setPort(10002).setRoomNo(String.valueOf(runQuicklyRoom.getRoomNo())).build();
                                                    if (userList.size() >= 4) {
                                                        runQuicklyRoom.addSeats(userList.subList(0, 4));
                                                        for (int i = 0; i < 4; i++) {
                                                            redisService.addCache("reconnect" + userList.get(i).getUserId(), "run_quickly," + runQuicklyRoom.getRoomNo());
                                                            if (HallTcpService.userClients.containsKey(userList.get(i).getUserId())) {
                                                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.COMPETITION_START).setData(roomResponse.toByteString()).build(), userList.get(i).getUserId());
                                                            }
                                                        }
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                    }
                                                    roomNos.add(Integer.parseInt(runQuicklyRoom.getRoomNo()));
                                                    redisService.addCache("room" + runQuicklyRoom.getRoomNo(), JSON.toJSONString(runQuicklyRoom));

                                                    redisService.addCache("room_type" + runQuicklyRoom.getRoomNo(), "run_quickly");
                                                    redisService.addCache("room_match" + runQuicklyRoom.getRoomNo(), String.valueOf(matchNo));
                                                    break;
                                                case RUIJIN_MAHJONG:
                                                    RuijinMahjongRoom ruijinMahjongRoom = new RuijinMahjongRoom(1, roomNo(), userList.get(0).getUserId(), 1, 4, true, 1);
                                                    roomResponse = Hall.RoomResponse.newBuilder().setIntoIp(Constant.gameServerIp)
                                                            .setPort(10003).setRoomNo(String.valueOf(ruijinMahjongRoom.getRoomNo())).build();
                                                    if (userList.size() >= 4) {
                                                        ruijinMahjongRoom.addSeats(userList.subList(0, 4));
                                                        for (int i = 0; i < 4; i++) {
                                                            redisService.addCache("reconnect" + userList.get(i).getUserId(), "ruijin_mahjong," + ruijinMahjongRoom.getRoomNo());
                                                            if (HallTcpService.userClients.containsKey(userList.get(i).getUserId())) {
                                                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.COMPETITION_START).setData(roomResponse.toByteString()).build(), userList.get(i).getUserId());
                                                            }
                                                        }
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                    }
                                                    roomNos.add(Integer.parseInt(ruijinMahjongRoom.getRoomNo()));
                                                    redisService.addCache("room" + ruijinMahjongRoom.getRoomNo(), JSON.toJSONString(ruijinMahjongRoom));

                                                    redisService.addCache("room_type" + ruijinMahjongRoom.getRoomNo(), "ruijin_mahjong");
                                                    redisService.addCache("room_match" + ruijinMahjongRoom.getRoomNo(), String.valueOf(matchNo));
                                                    break;
                                                case SANGONG:
                                                    SangongRoom sangongRoom = new SangongRoom(1, String.valueOf(roomNo()), 1, 1, 0, 0, 4);
                                                    roomResponse = Hall.RoomResponse.newBuilder().setIntoIp(Constant.gameServerIp)
                                                            .setPort(10004).setRoomNo(String.valueOf(sangongRoom.getRoomNo())).build();

                                                    if (userList.size() >= 4) {
                                                        sangongRoom.addSeats(userList.subList(0, 4));
                                                        for (int i = 0; i < 4; i++) {
                                                            redisService.addCache("reconnect" + userList.get(i).getUserId(), "sangong," + sangongRoom.getRoomNo());
                                                            if (HallTcpService.userClients.containsKey(userList.get(i).getUserId())) {
                                                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.COMPETITION_START).setData(roomResponse.toByteString()).build(), userList.get(i).getUserId());
                                                            }
                                                        }
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                        userList.remove(0);
                                                    }
                                                    roomNos.add(Integer.parseInt(sangongRoom.getRoomNo()));
                                                    redisService.addCache("room" + sangongRoom.getRoomNo(), JSON.toJSONString(sangongRoom));

                                                    redisService.addCache("room_type" + sangongRoom.getRoomNo(), "sangong");
                                                    redisService.addCache("room_match" + sangongRoom.getRoomNo(), String.valueOf(matchNo));
                                                    break;
                                            }
                                        }

                                        MatchInfo matchInfo = new MatchInfo();
                                        matchInfo.setStatus(1);
                                        matchInfo.setMatchEliminateScore(990);
                                        matchInfo.setRooms(roomNos);
                                        matchInfo.setArena(arena);
                                        matchInfo.setMatchUsers(matchUsers);
                                        matchInfo.setStart(false);
                                        redisService.addCache("match_info" + matchNo, JSON.toJSONString(matchInfo));
                                    }
                                    redisService.delete("registration_population" + registrationRequest.getId());
                                    redisService.unlock("lock_registration_population" + registrationRequest.getId());
                                    break;
                                } else {
                                    redisService.addCache("registration_population" + registrationRequest.getId(), JSON.toJSONString(people));
                                }
                            }
                            redisService.unlock("lock_registration_population" + registrationRequest.getId());
                        } else {
                            List<Integer> people = new ArrayList<>();
                            people.add(userId);
                            redisService.addCache("registration_population" + registrationRequest.getId(), JSON.toJSONString(people));
                            registrationResponse.setError(GameBase.ErrorCode.SUCCESS);
                        }
                    } else {
                        registrationResponse.setError(GameBase.ErrorCode.ROOM_NOT_EXIST);
                    }
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.REGISTRATION).setData(registrationResponse.build().toByteString()).build(), userId);
                    break;
                case LOGGER:
                    GameBase.LoggerRequest loggerRequest = GameBase.LoggerRequest.parseFrom(request.getData());
                    LoggerUtil.logger(userId + "----" + loggerRequest.getLogger());
                    break;
                case EXCHANGE_HISTORY:
                    Hall.ExchangeHistory.Builder exchangeHistory = Hall.ExchangeHistory.newBuilder();
                    ApiResponse<List<Exchange>> exchangeHistoryResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.exchangeListUrl + userId, null), new TypeReference<ApiResponse<List<Exchange>>>() {
                    });
                    if (null != exchangeHistoryResponse && 0 == exchangeHistoryResponse.getCode()) {
                        if (null != exchangeHistoryResponse.getData() && 0 < exchangeHistoryResponse.getData().size()) {
                            for (Exchange exchange : exchangeHistoryResponse.getData()) {
                                exchangeHistory.addExchangeHistory(Hall.ExchangeHistoryItem.newBuilder()
                                        .setDateTime(exchange.getCreateDate().getTime()).setGolds(exchange.getPrice()).setGoodsName(exchange.getGoodsName()));
                            }
                        }
                    }
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.EXCHANGE_HISTORY).setData(exchangeHistory.build().toByteString()).build(), userId);
                    break;
                case MALL:
                    Hall.Mall.Builder mall = Hall.Mall.newBuilder();
                    mall.setWechat("121212");

                    if (redisService.exists("cache_goods")) {
                        String goodsCache = redisService.getCache("cache_goods");
                        int version = Integer.parseInt(goodsCache.substring(0, goodsCache.indexOf(",")));
                        if (this.version != version) {
                            this.version = version;
                            mall.setUpdate(true);
                            List<Goods> goodsList = JSON.parseArray(goodsCache.substring(goodsCache.indexOf(",") + 1), Goods.class);
                            if (null != goodsList && 0 < goodsList.size()) {
                                for (Goods goods : goodsList) {
                                    mall.addGoods(Hall.Goods.newBuilder().setGolds(goods.getPrice()).setGoodsName(goods.getName()).setImageUrl(goods.getImg()));
                                }
                            }
                        }
                    }
                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.MALL).setData(mall.build().toByteString()).build(), userId);
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
        if (redisService.exists("room" + roomNo + "")) {
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

    /**
     * 游戏记录
     *
     * @return
     */
    private GameBase.RecordResponse gameRecord() {
        GameBase.RecordResponse.Builder recordResponse = GameBase.RecordResponse.newBuilder();
        jsonObject.clear();
        jsonObject.put("userId", userId);
        ApiResponse<List<GameRecordRepresentation>> gameRecordResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.gamerecordListUrl, jsonObject.toJSONString()),
                new TypeReference<ApiResponse<List<GameRecordRepresentation>>>() {
                });
        Map<GameType, GameBase.GameRecord.Builder> gameRecords = new HashMap<>();
        if (0 == gameRecordResponse.getCode()) {
            for (GameRecordRepresentation gameRecordRepresentation : gameRecordResponse.getData()) {
                if (!gameRecords.containsKey(gameRecordRepresentation.getGameType())) {
                    gameRecords.put(gameRecordRepresentation.getGameType(), GameBase.GameRecord.newBuilder()
                            .setGameType(GameBase.GameType.forNumber(gameRecordRepresentation.getGameType().ordinal())));
                }
                GameBase.Record.Builder record = GameBase.Record.newBuilder();
                record.setRecordId(gameRecordRepresentation.getId());
                record.setRoomNo(gameRecordRepresentation.getRoomNo() + "");
                record.setGameCount(gameRecordRepresentation.getGameCount());
                record.setDateTime(gameRecordRepresentation.getCreateDate().getTime());
                if (null != gameRecordRepresentation.getsData()) {
                    List<TotalScore> totalScores = JSON.parseArray(new String(gameRecordRepresentation.getsData(), Charset.forName("utf-8")), TotalScore.class);
                    for (TotalScore totalScore : totalScores) {
                        record.addUserRecord(GameBase.UserRecord.newBuilder().setNickname(totalScore.getNickname())
                                .setHead(totalScore.getHead()).setID(totalScore.getUserId()).setScore(totalScore.getScore()));
                    }
                }
                gameRecords.get(gameRecordRepresentation.getGameType()).addRecords(record);
            }
            gameRecords.forEach(new BiConsumer<GameType, GameBase.GameRecord.Builder>() {
                @Override
                public void accept(GameType gameType, GameBase.GameRecord.Builder builder) {
                    recordResponse.addGameRecords(builder);
                }
            });
        }
        return recordResponse.build();
    }

    /**
     * 记录详情
     *
     * @param recordId
     * @return
     */
    private GameBase.RecordDetailsResponse recordDetails(String recordId) {
        GameBase.RecordDetailsResponse.Builder recordResponse = GameBase.RecordDetailsResponse.newBuilder();
        ApiResponse<GameRecordInfoRepresentation> gameRecordResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.gamerecordInfoUrl + recordId, null),
                new TypeReference<ApiResponse<GameRecordInfoRepresentation>>() {
                });
        if (0 == gameRecordResponse.getCode()) {
            recordResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);
            if (null != gameRecordResponse.getData().getData()) {
                List<Record> records = JSON.parseArray(new String(gameRecordResponse.getData().getData(), Charset.forName("utf-8")), Record.class);
                for (Record record : records) {
                    GameBase.RoundItemRecord.Builder roundItemRecord = GameBase.RoundItemRecord.newBuilder();
                    for (SeatRecord seatRecord : record.getSeatRecordList()) {
                        roundItemRecord.addUserRecord(GameBase.UserRecord.newBuilder().setID(seatRecord.getUserId())
                                .setNickname(seatRecord.getNickname()).setHead(seatRecord.getNickname()).setScore(seatRecord.getWinOrLose()).build());
                    }
                    recordResponse.addRoundItemRecord(roundItemRecord);
                }
            }
        } else {
            recordResponse.setErrorCode(GameBase.ErrorCode.ERROR_UNKNOW);
        }
        return recordResponse.build();
    }

    /**
     * 回放
     *
     * @param recordId
     * @param round
     * @return
     */
    private GameBase.ReplayResponse replay(String recordId, int round) {
        GameBase.ReplayResponse.Builder replayResponse = GameBase.ReplayResponse.newBuilder();
        ApiResponse<GameRecordInfoRepresentation> gameRecordResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.gamerecordInfoUrl + recordId, null),
                new TypeReference<ApiResponse<GameRecordInfoRepresentation>>() {
                });
        if (0 == gameRecordResponse.getCode()) {
            replayResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);

            switch (gameRecordResponse.getData().getGameType()) {
                case XINGNING_MAHJONG:
                    if (null != gameRecordResponse.getData().getData()) {
                        List<Record> records = JSON.parseArray(new String(gameRecordResponse.getData().getData(), Charset.forName("utf-8")), Record.class);
                        Record record = records.get(round);

                        Xingning.XingningMahjongReplayResponse.Builder xingningReplayResponse = Xingning.XingningMahjongReplayResponse.newBuilder();
                        Mahjong.MahjongStartResponse.Builder dealCard = Mahjong.MahjongStartResponse.newBuilder();
                        dealCard.setBanker(record.getBanker()).addAllDice(Arrays.asList(record.getDice()));
                        xingningReplayResponse.setStart(dealCard);

                        for (OperationHistory operationHistory : record.getHistoryList()) {
                            GameBase.OperationHistory.Builder builder = GameBase.OperationHistory.newBuilder();
                            builder.setID(operationHistory.getUserId());
                            builder.addCard(operationHistory.getCard());
                            switch (operationHistory.getHistoryType()) {
                                case GET_CARD:
                                    builder.setOperationId(GameBase.ActionId.GET_CARD);
                                    break;
                                case PLAY_CARD:
                                    builder.setOperationId(GameBase.ActionId.PLAY_CARD);
                                    break;
                                case PENG:
                                    builder.setOperationId(GameBase.ActionId.PENG);
                                    break;
                                case AN_GANG:
                                    builder.setOperationId(GameBase.ActionId.AN_GANG);
                                    break;
                                case DIAN_GANG:
                                    builder.setOperationId(GameBase.ActionId.DIAN_GANG);
                                    break;
                                case BA_GANG:
                                    builder.setOperationId(GameBase.ActionId.BA_GANG);
                                    break;
                                case HU:
                                    builder.setOperationId(GameBase.ActionId.HU);
                                    break;
                                case CHI:
                                    builder.setOperationId(GameBase.ActionId.CHI);
                                    break;
                            }
                            xingningReplayResponse.addHistory(builder);
                        }
                        replayResponse.setReplay(xingningReplayResponse.build().toByteString());
                    }
                    break;
                case RUN_QUICKLY:
                    break;
                case RUIJIN_MAHJONG:
                    if (null != gameRecordResponse.getData().getData()) {
                        List<Record> records = JSON.parseArray(new String(gameRecordResponse.getData().getData(), Charset.forName("utf-8")), Record.class);
                        Record record = records.get(round);

                        Ruijin.RuijinMahjongReplayResponse.Builder ruijinReplayResponse = Ruijin.RuijinMahjongReplayResponse.newBuilder();
                        Mahjong.MahjongStartResponse.Builder dealCard = Mahjong.MahjongStartResponse.newBuilder();
                        Ruijin.RuijinStartResponse.Builder ruijinDealCard = Ruijin.RuijinStartResponse.newBuilder();
                        dealCard.setBanker(record.getBanker()).addAllDice(Arrays.asList(record.getDice()));
                        ruijinDealCard.setRogue(record.getJiabao());
                        ruijinReplayResponse.setStart(ruijinDealCard);

                        for (OperationHistory operationHistory : record.getHistoryList()) {
                            GameBase.OperationHistory.Builder builder = GameBase.OperationHistory.newBuilder();
                            builder.setID(operationHistory.getUserId());
                            builder.addCard(operationHistory.getCard());
                            switch (operationHistory.getHistoryType()) {
                                case GET_CARD:
                                    builder.setOperationId(GameBase.ActionId.GET_CARD);
                                    break;
                                case PLAY_CARD:
                                    builder.setOperationId(GameBase.ActionId.PLAY_CARD);
                                    break;
                                case PENG:
                                    builder.setOperationId(GameBase.ActionId.PENG);
                                    break;
                                case AN_GANG:
                                    builder.setOperationId(GameBase.ActionId.AN_GANG);
                                    break;
                                case DIAN_GANG:
                                    builder.setOperationId(GameBase.ActionId.DIAN_GANG);
                                    break;
                                case BA_GANG:
                                    builder.setOperationId(GameBase.ActionId.BA_GANG);
                                    break;
                                case HU:
                                    builder.setOperationId(GameBase.ActionId.HU);
                                    break;
                            }
                            ruijinReplayResponse.addHistory(builder);
                        }
                    }
                    break;
                case SANGONG:
                    break;
            }
        } else {
            replayResponse.setErrorCode(GameBase.ErrorCode.ERROR_UNKNOW);
        }
        return replayResponse.build();
    }

}
