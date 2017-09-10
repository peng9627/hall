import com.google.protobuf.GeneratedMessageV3;
import game.mode.GameBase;
import game.mode.Hall;
import game.utils.ByteUtils;
import game.utils.CoreStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RobotThread implements Runnable {

    private byte[] md5Key = "2704031cd4814eb2a82e47bd1d9042c6".getBytes();
    private Logger logger = LoggerFactory.getLogger(RobotThread.class);

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

    private void send(OutputStream os, GeneratedMessageV3 messageV3) {
        try {
            String md5 = CoreStringUtils.md5(ByteUtils.addAll(md5Key, messageV3.toByteArray()), 32, false);
            messageV3.sendTo(os, md5);
            logger.info(id + "发送" + messageV3.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String ip;
    int port;
    int id;
    String roomNo;

    public RobotThread(String ip, int port, int id, String roomNo) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.roomNo = roomNo;
    }

    @Override
    public void run() {
        try {
            GameBase.BaseConnection.Builder request;
            GameBase.BaseAction.Builder action;
            GameBase.BaseConnection response;
            InputStream is;
            OutputStream os;

            Socket socket = new Socket(ip, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            request = GameBase.BaseConnection.newBuilder();
            action = GameBase.BaseAction.newBuilder();

            for (int i = 0; i < 16; i++) {
                Hall.LoginRequest loginRequest = Hall.LoginRequest.newBuilder().setUsername("test" + 0 + "" + i).setNickname("test" + 0 + "" + i).setHead("")
                        .setAgent(Hall.Agent.ANDROID).setSex(true).build();
                send(os, request.setOperationType(GameBase.OperationType.LOGIN).setData(loginRequest.toByteString()).build());

                Hall.RegistrationRequest registrationRequest = Hall.RegistrationRequest.newBuilder().setId("ff8080115df48669015da48a1d7a0000").build();
                send(os, request.setOperationType(GameBase.OperationType.REGISTRATION).setData(registrationRequest.toByteString()).build());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}