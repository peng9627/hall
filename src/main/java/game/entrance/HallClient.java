package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.InvalidProtocolBufferException;
import game.constant.Constant;
import game.mode.*;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
                                reconnect.setIntoIp(Constant.gameServerIp);
                                reconnect.setPort(10005);
                                messageReceive.send(this.response.setOperationType(GameBase.OperationType.RECONNECTION).setData(reconnect.build().toByteString()).build(), userId);
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
                        Hall.XingningMahjongCreateRoomRequest createRoomRequest = Hall.XingningMahjongCreateRoomRequest.parseFrom(request.getData());
                        jsonObject.clear();
                        jsonObject.put("userId", userId);
                        jsonObject.put("flowType", 2);
                        XingningMahjongRoom xingningMahjongRoom = new XingningMahjongRoom(createRoomRequest.getBaseScore(),
                                roomNo(), userId, createRoomRequest.getGameTimes(), createRoomRequest.getCount(),
                                createRoomRequest.getMaCount(), createRoomRequest.getGhost(), createRoomRequest.getGameRules());
                        Hall.RoomResponse.Builder createRoomResponse = Hall.RoomResponse.newBuilder();
                        if (0 == xingningMahjongRoom.getCount()) {
                            createRoomResponse.setRoomNo(xingningMahjongRoom.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW)
                                    .setIntoIp(Constant.gameServerIp).setPort(10001).build();
                            messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                            break;
                        }
                        jsonObject.put("description", "开房间" + xingningMahjongRoom.getRoomNo());
                        if (8 == createRoomRequest.getGameTimes()) {
                            jsonObject.put("money", 1);
                        } else {
                            jsonObject.put("money", 2);
                        }
                        if (userResponse.getData().getMoney() < jsonObject.getIntValue("money")) {
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
                        if (jsonObject.containsKey("money")) {
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
                        XingningMahjongRoom xingningMahjongRoom = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), XingningMahjongRoom.class);

                        if (0 != xingningMahjongRoom.getGameStatus().compareTo(GameStatus.WAITING)) {
                            createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                        } else {
                            createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                    .setIntoIp(Constant.gameServerIp).setPort(10001);
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
        jsonObject.put("gameType", 4);
        jsonObject.put("userId", userId);
        ApiResponse<List<GameRecordRepresentation>> gameRecordResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.gamerecordListUrl, jsonObject.toJSONString()),
                new TypeReference<ApiResponse<List<GameRecordRepresentation>>>() {
                });
        if (0 == gameRecordResponse.getCode()) {
            for (GameRecordRepresentation gameRecordRepresentation : gameRecordResponse.getData()) {
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
                recordResponse.addRecords(record);
            }
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

                        Songjianghe.SongjiangheMahjongReplayResponse.Builder sonfjiangheReplayResponse = Songjianghe.SongjiangheMahjongReplayResponse.newBuilder();
                        Mahjong.MahjongStartResponse.Builder dealCard = Mahjong.MahjongStartResponse.newBuilder();
                        dealCard.setBanker(record.getBanker()).addAllDice(Arrays.asList(record.getDice()));
                        sonfjiangheReplayResponse.setStart(dealCard);

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
                            sonfjiangheReplayResponse.addHistory(builder);
                        }
                        replayResponse.setReplay(sonfjiangheReplayResponse.build().toByteString());
                    }
                    break;
            }
        } else {
            replayResponse.setErrorCode(GameBase.ErrorCode.ERROR_UNKNOW);
        }
        return replayResponse.build();
    }

}
