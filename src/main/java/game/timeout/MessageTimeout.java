package game.timeout;

import game.constant.Constant;
import game.entrance.MessageReceive;

import java.util.Date;

/**
 * Created by pengyi
 * Date : 17-9-13.
 * desc:
 */
public class MessageTimeout extends Thread {

    private Date lastMessageDate;
    private MessageReceive messageReceive;

    public MessageTimeout(Date lastMessageDate, MessageReceive messageReceive) {
        this.lastMessageDate = lastMessageDate;
        this.messageReceive = messageReceive;
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

        if (0 == messageReceive.lastMessageDate.compareTo(lastMessageDate)) {
            messageReceive.close();
        }
    }
}
