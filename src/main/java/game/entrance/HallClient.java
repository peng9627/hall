package game.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.InvalidProtocolBufferException;
import game.constant.Constant;
import game.mode.*;
import game.mode.songjianghe.*;
import game.redis.RedisService;
import game.utils.HttpUtil;
import game.utils.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

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
                            if (redisService.exists("room" + reconnectInfo[1]) && "songjianghe_mahjong".equals(reconnectInfo[0])) {
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
                                reconnect.setPort(10101);
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
                        sendAgentRoomList();
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
                        Hall.SongjiangheCreateRoomRequest createRoomRequest = Hall.SongjiangheCreateRoomRequest.parseFrom(request.getData());
                        jsonObject.clear();
                        jsonObject.put("userId", userId);
                        jsonObject.put("flowType", 2);
                        Room room = new Room(roomNo(), userId, createRoomRequest.getGameTimes(), createRoomRequest.getCount(), createRoomRequest.getGameRules());
                        Hall.RoomResponse.Builder createRoomResponse = Hall.RoomResponse.newBuilder();
                        if (0 == room.getCount()) {
                            createRoomResponse.setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.ERROR_UNKNOW)
                                    .setIntoIp(Constant.gameServerIp).setPort(10101).build();
                            messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                            break;
                        }
                        jsonObject.put("description", "开房间" + room.getRoomNo());
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
                        redisService.addCache("room" + room.getRoomNo(), JSON.toJSONString(room));
                        redisService.addCache("room_type" + room.getRoomNo(), "songjianghe_mahjong");

                        if (1 == (createRoomRequest.getGameRules() >> 4) % 2) {
                            createRoomResponse.setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.AGENT_SUCCESS)
                                    .setIntoIp(Constant.gameServerIp).setPort(10101).build();
                        } else {
                            createRoomResponse.setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                    .setIntoIp(Constant.gameServerIp).setPort(10101).build();
                        }

                        messageReceive.send(this.response.setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.build().toByteString()).build(), userId);
                        if (jsonObject.containsKey("money")) {
                            ApiResponse moneyDetail = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.moneyDetailedCreate, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
                            });
                            if (0 != moneyDetail.getCode()) {
                                LoggerFactory.getLogger(this.getClass()).error(Constant.apiUrl + Constant.moneyDetailedCreate + "?" + jsonObject.toJSONString());
                            }
                        }
                        if (1 == (createRoomRequest.getGameRules() >> 4) % 2) {
                            while (!redisService.lock("lock_agent_rooms" + userId)) {
                            }
                            List<String> agentRooms;
                            if (redisService.exists("agent_rooms" + userId)) {
                                agentRooms = JSON.parseArray(redisService.getCache("agent_rooms" + userId), String.class);
                            } else {
                                agentRooms = new ArrayList<>();
                            }
                            agentRooms.add(room.getRoomNo());
                            redisService.addCache("agent_rooms" + userId, JSON.toJSONString(agentRooms));
                            redisService.unlock("lock_agent_rooms" + userId);
                            sendAgentRoomList();
                        } else {
                            redisService.addCache("reconnect" + userId, "songjianghe_mahjong," + room.getRoomNo());
                        }
                    }
                    break;
                case ADD_ROOM:
                    Hall.AddToRoomRequest addToRoomRequest = Hall.AddToRoomRequest.parseFrom(request.getData());
                    Hall.RoomResponse.Builder createRoomResponse = Hall.RoomResponse.newBuilder();
                    if (redisService.exists("room" + addToRoomRequest.getRoomNo())) {
                        Room room = JSON.parseObject(redisService.getCache("room" + addToRoomRequest.getRoomNo()), Room.class);

                        boolean sameIp = false;
                        if (0 == (room.getGameRules() >> 3) % 2) {
                            for (Seat seat : room.getSeats()) {
                                if (seat.getIp().equals(ip)) {
                                    messageReceive.send(this.response.setOperationType(GameBase.OperationType.ADD_ROOM).setData(Hall.RoomResponse.newBuilder()
                                            .setError(GameBase.ErrorCode.SAME_IP).build().toByteString()).build(), userId);
                                    sameIp = true;
                                    break;
                                }
                            }
                        }
                        if (sameIp) {
                            break;
                        }
                        if (0 != room.getGameStatus().compareTo(GameStatus.WAITING)) {
                            createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.GAME_START);
                        } else {
                            createRoomResponse.setRoomNo(addToRoomRequest.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS)
                                    .setIntoIp(Constant.gameServerIp).setPort(10101);
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
                        sendAgentRoomList();
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
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    private void sendAgentRoomList() {
        while (!redisService.lock("lock_agent_rooms" + userId)) {
        }
        List<String> agentRooms;
        if (redisService.exists("agent_rooms" + userId)) {
            agentRooms = JSON.parseArray(redisService.getCache("agent_rooms" + userId), String.class);
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
        response.setOperationType(GameBase.OperationType.AGENT_ROOM_LIST).setData(agentRoomList.build().toByteString());
        messageReceive.send(response.build(), userId);
        if (0 < agentRooms.size()) {
            redisService.addCache("agent_rooms" + userId, JSON.toJSONString(agentRooms));
        } else {
            redisService.delete("agent_rooms" + userId);
        }
        redisService.unlock("lock_agent_rooms" + userId);
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
                    GameBase.RoundItemRecord.Builder roundItemRecord = GameBase.RoundItemRecord.newBuilder().setDateTime(record.getStartDate().getTime());
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
            GameRecordInfoRepresentation infoRepresentation = gameRecordResponse.getData();
            switch (infoRepresentation.getGameType()) {
                case SONGJIANGHE:
                    if (null != infoRepresentation.getData()) {
                        List<Record> records = JSON.parseArray(new String(infoRepresentation.getData(), Charset.forName("utf-8")), Record.class);
                        Record record = records.get(round);

                        Mahjong.MahjongReplayResponse.Builder sonfjiangheReplayResponse = Mahjong.MahjongReplayResponse.newBuilder();

                        Mahjong.GameInitInfo.Builder gameInfo = Mahjong.GameInitInfo.newBuilder();
                        gameInfo.setSurplusCardsSize(135 - (record.getSeatRecordList().size() * 13));
                        gameInfo.setBanker(record.getBanker());
                        if (null != record.getDice() && 0 < record.getDice().length) {
                            gameInfo.addAllDice(Arrays.asList(record.getDice()));
                        }
                        gameInfo.setGameCount(record.getGameCount());

                        Mahjong.MahjongResultResponse.Builder resultResponse = Mahjong.MahjongResultResponse.newBuilder();
                        GameBase.RoomSeatsInfo.Builder roomSeatsInfo = GameBase.RoomSeatsInfo.newBuilder();
                        for (SeatRecord seatRecord : record.getSeatRecordList()) {
                            Mahjong.MahjongSeatGameInitInfo.Builder gameSeatResponse = Mahjong.MahjongSeatGameInitInfo.newBuilder();
                            Mahjong.MahjongUserResult.Builder mahjongUserResult = Mahjong.MahjongUserResult.newBuilder();
                            GameBase.SeatResponse.Builder seatResponse = GameBase.SeatResponse.newBuilder();
                            gameSeatResponse.setID(seatRecord.getUserId());
                            if (null != seatRecord.getInitialCards()) {
                                gameSeatResponse.addAllInitialCards(seatRecord.getInitialCards());
                            }
                            gameInfo.addSeats(gameSeatResponse);
                            mahjongUserResult.setID(seatRecord.getUserId());
                            mahjongUserResult.setScore(seatRecord.getScore());
                            mahjongUserResult.setWinOrLose(seatRecord.getWinOrLose());
                            if (null != seatRecord.getCardResult()) {
                                mahjongUserResult.setCardScore(seatRecord.getCardResult().getScore());
                                for (ScoreType scoreType : seatRecord.getCardResult().getScoreTypes()) {
                                    mahjongUserResult.addScoreTypes(Mahjong.ScoreType.forNumber(scoreType.ordinal() - 3));
                                }
                            }
                            int mingGangScore = 0;
                            for (GameResult gameResult : seatRecord.getMingGangResult()) {
                                mingGangScore += gameResult.getScore();
                            }
                            int anGangScore = 0;
                            for (GameResult gameResult : seatRecord.getAnGangResult()) {
                                anGangScore += gameResult.getScore();
                            }
                            int xfGangScore = 0;
                            for (GameResult gameResult : seatRecord.getXfGangResult()) {
                                xfGangScore += gameResult.getScore();
                            }
                            mahjongUserResult.setGangScore(mingGangScore + anGangScore + xfGangScore);
                            if (null != seatRecord.getChiCards()) {
                                mahjongUserResult.addAllChiCards(seatRecord.getChiCards());
                            }
                            if (null != seatRecord.getPengCards()) {
                                mahjongUserResult.addAllPengCards(seatRecord.getPengCards());
                            }
                            if (null != seatRecord.getMingGangCards()) {
                                mahjongUserResult.addAllMingGangCards(seatRecord.getMingGangCards());
                            }
                            if (null != seatRecord.getAnGangCards()) {
                                mahjongUserResult.addAllAnGangCards(seatRecord.getAnGangCards());
                            }
                            if (null != seatRecord.getCards()) {
                                mahjongUserResult.addAllCards(seatRecord.getCards());
                            }
                            if (null != seatRecord.getXfGangCards()) {
                                mahjongUserResult.addAllXuanfengGangCards(seatRecord.getXfGangCards());
                            }
                            mahjongUserResult.setHuCard(seatRecord.getHuCard());
                            resultResponse.addUserResult(mahjongUserResult);

                            seatResponse.setSeatNo(seatRecord.getSeatNo());
                            seatResponse.setID(seatRecord.getUserId());
                            seatResponse.setScore(seatRecord.getScore() - seatRecord.getWinOrLose());
                            seatResponse.setReady(false);
                            seatResponse.setNickname(seatRecord.getNickname());
                            seatResponse.setHead(seatRecord.getHead());
                            seatResponse.setSex(seatRecord.isSex());
                            seatResponse.setOffline(false);
                            seatResponse.setIsRobot(false);
                            seatResponse.setIp(seatRecord.getIp());
                            roomSeatsInfo.addSeats(seatResponse.build());
                        }
                        sonfjiangheReplayResponse.setGameInitInfo(gameInfo);
                        sonfjiangheReplayResponse.setResult(resultResponse);
                        sonfjiangheReplayResponse.setSeatInfo(roomSeatsInfo);

                        for (OperationHistory operationHistory : record.getHistoryList()) {
                            GameBase.BaseAction.Builder builder = GameBase.BaseAction.newBuilder();
                            builder.setID(operationHistory.getUserId());
                            switch (operationHistory.getHistoryType()) {
                                case GET_CARD:
                                    builder.setOperationId(GameBase.ActionId.GET_CARD);
                                    builder.setData(Mahjong.MahjongGetCardResponse.newBuilder().setCard(operationHistory.getCards().get(0)).build().toByteString());
                                    break;
                                case PLAY_CARD:
                                    builder.setOperationId(GameBase.ActionId.PLAY_CARD);
                                    builder.setData(Mahjong.MahjongPlayCard.newBuilder().setCard(operationHistory.getCards().get(0)).build().toByteString());
                                    break;
                                case PENG:
                                    builder.setOperationId(GameBase.ActionId.PENG);
                                    builder.setData(Mahjong.MahjongPengResponse.newBuilder().setCard(operationHistory.getCards().get(0)).build().toByteString());
                                    break;
                                case AN_GANG:
                                    builder.setOperationId(GameBase.ActionId.AN_GANG);
                                    builder.setData(Mahjong.MahjongGang.newBuilder().addAllCard(operationHistory.getCards()).build().toByteString());
                                    break;
                                case DIAN_GANG:
                                    builder.setOperationId(GameBase.ActionId.DIAN_GANG);
                                    builder.setData(Mahjong.MahjongGang.newBuilder().addAllCard(operationHistory.getCards()).build().toByteString());
                                    break;
                                case BA_GANG:
                                    builder.setOperationId(GameBase.ActionId.BA_GANG);
                                    builder.setData(Mahjong.MahjongGang.newBuilder().addAllCard(operationHistory.getCards()).build().toByteString());
                                    break;
                                case HU:
                                    builder.setOperationId(GameBase.ActionId.HU);
                                    builder.setData(Mahjong.MahjongHuResponse.newBuilder().setCard(operationHistory.getCards().get(0)).build().toByteString());
                                    break;
                                case CHI:
                                    builder.setOperationId(GameBase.ActionId.CHI);
                                    builder.setData(Mahjong.MahjongChi.newBuilder().addAllCards(operationHistory.getCards()).build().toByteString());
                                    break;
                                case XF_GANG:
                                    builder.setOperationId(GameBase.ActionId.XF_GANG);
                                    builder.setData(Mahjong.MahjongGang.newBuilder().addAllCard(operationHistory.getCards()).build().toByteString());
                                    break;
                            }
                            sonfjiangheReplayResponse.addHistory(builder);
                        }

                        GameBase.RoomCardIntoResponse.Builder roomCardIntoResponseBuilder = GameBase.RoomCardIntoResponse.newBuilder();
                        roomCardIntoResponseBuilder.setRoomNo(infoRepresentation.getRoomNo().toString());
                        roomCardIntoResponseBuilder.setRoomOwner(infoRepresentation.getRoomOwner());
                        roomCardIntoResponseBuilder.setStarted(true);
                        Songjianghe.SongjiangheMahjongIntoResponse.Builder intoResponseBuilder = Songjianghe.SongjiangheMahjongIntoResponse.newBuilder();
                        intoResponseBuilder.setCount(infoRepresentation.getPeopleCount());
                        intoResponseBuilder.setGameTimes(infoRepresentation.getGameTotal());
                        JSONObject jsonObject = JSON.parseObject(infoRepresentation.getGameRule());
                        intoResponseBuilder.setGameRules(jsonObject.getIntValue("gameRule"));
                        intoResponseBuilder.setNormal(jsonObject.getBooleanValue("normal"));
                        intoResponseBuilder.setSingleFan(jsonObject.getBooleanValue("singleFan"));
                        roomCardIntoResponseBuilder.setError(GameBase.ErrorCode.SUCCESS);
                        roomCardIntoResponseBuilder.setData(intoResponseBuilder.build().toByteString());
                        sonfjiangheReplayResponse.setRoomInfo(roomCardIntoResponseBuilder);
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
