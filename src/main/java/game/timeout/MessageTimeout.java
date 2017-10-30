package game.timeout;

import game.constant.Constant;
import game.entrance.HallTcpService;

import java.util.Date;

/**
 * Created by pengyi
 * Date : 17-9-13.
 * desc:
 */
public class MessageTimeout extends Thread {

    private Date lastMessageDate;
    private int userId;

    public MessageTimeout(Date lastMessageDate, int userId) {
        this.lastMessageDate = lastMessageDate;
        this.userId = userId;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                wait(Constant.messageTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (HallTcpService.userClients.containsKey(userId)
                && 0 == HallTcpService.userClients.get(userId).lastMessageDate.compareTo(lastMessageDate)) {
            HallTcpService.userClients.get(userId).close();
        }
    }
}
