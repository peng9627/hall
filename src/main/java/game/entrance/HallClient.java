package game.entrance;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.MessageLite;
import game.mode.GameBase;
import game.mode.Hall;
import game.mode.Room;
import game.redis.RedisService;
import game.utils.ByteUtils;
import game.utils.CoreStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class HallClient implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket s;
    private String username;
    private final InputStream is;
    private final OutputStream os;

    private RedisService redisService;
    private double score;
    private Boolean connect;
    private byte[] md5Key = "2704031cd4814eb2a82e47bd1d9042c6".getBytes();

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

    public boolean send(MessageLite lite, String username) {
        try {
            if (HallTcpService.userClients.containsKey(username)) {
                synchronized (HallTcpService.userClients.get(username).os) {
                    OutputStream os = HallTcpService.userClients.get(username).os;
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
            if (null != username) {
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
        os.write((value >>> 24) & 0xFF);
        os.write((value >>> 16) & 0xFF);
        os.write((value >>> 8) & 0xFF);
        os.write((value) & 0xFF);
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
                            username = loginRequest.getUsername();
                            HallTcpService.userClients.put(loginRequest.getUsername(), this);
                            Hall.LoginResponse.Builder loginResponse = Hall.LoginResponse.newBuilder();
                            loginResponse.setErrorCode(GameBase.ErrorCode.SUCCESS);
                            loginResponse.setUsername(loginRequest.getUsername());
                            loginResponse.setNickname(null == loginRequest.getNickname() ? "" : loginRequest.getUsername());
                            loginResponse.setHead(null == loginRequest.getHead() ? "" : loginRequest.getHead());
                            loginResponse.setLastLoginDate(new Date().getTime());
                            loginResponse.setLastLoginIp(s.getInetAddress().getHostAddress());
                            loginResponse.setLastLoginAgent(loginRequest.getAgent());
                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.LOGIN).setData(loginResponse.build().toByteString()).build(), loginRequest.getUsername());
                            break;
                        case CREATE_ROOM:
                            Hall.CreateRoomRequest createRoomRequest = Hall.CreateRoomRequest.parseFrom(request.getData());
                            Room room = new Room(createRoomRequest.getBaseScore(), roomNo(), username, createRoomRequest.getGameTimes(),
                                    createRoomRequest.getCount(), createRoomRequest.getDianpao());
                            redisService.addCache("room" + room.getRoomNo(), JSON.toJSONString(room));
                            Hall.CreateRoomResponse createRoomResponse = Hall.CreateRoomResponse.newBuilder()
                                    .setRoomNo(room.getRoomNo()).setError(GameBase.ErrorCode.SUCCESS).setIntoIp("192.168.2.99")
                                    .setPort(10001).build();
                            send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.CREATE_ROOM).setData(createRoomResponse.toByteString()).build(), username);
                            break;
                        case QUERY:
                            Hall.QueryRequest queryRequest = Hall.QueryRequest.parseFrom(request.getData());
                            switch (queryRequest.getOperationId()) {
                                case ROOM_LIST:
                                    Hall.RoomListResponse roomListResponse = Hall.RoomListResponse.newBuilder()
                                            .addRoomList(Hall.Room.newBuilder().setBaseScore(1).setMinIntoScore(10).setCount(5).setIntoIp("192.168.2.99").setPort(10001))
                                            .addRoomList(Hall.Room.newBuilder().setBaseScore(10).setMinIntoScore(100).setCount(3).setIntoIp("192.168.2.99").setPort(10001))
                                            .addRoomList(Hall.Room.newBuilder().setBaseScore(50).setMinIntoScore(500).setCount(2).setIntoIp("192.168.2.99").setPort(10001)).build();

                                    Hall.QueryResponse queryResponse = Hall.QueryResponse.newBuilder().setError(GameBase.ErrorCode.SUCCESS)
                                            .setOperationId(GameBase.OperationId.ROOM_LIST).setData(roomListResponse.toByteString()).build();
                                    send(GameBase.BaseConnection.newBuilder().setOperationType(GameBase.OperationType.QUERY).setData(queryResponse.toByteString()).build(), username);
                                    break;
                            }
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
        } catch (Exception e) {
            logger.info("socket.dirty.shutdown.message");
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
}
