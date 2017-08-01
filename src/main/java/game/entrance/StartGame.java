package game.entrance;

/**
 * Created by pengyi
 * Date 2017/7/25.
 */
public class StartGame {
    public static void main(String[] args) {
        new Thread(new HallTcpService()).start();
    }
}
