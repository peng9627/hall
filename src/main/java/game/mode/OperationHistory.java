package game.mode;

/**
 * Created by pengyi
 * Date 2017/7/28.
 */
public class OperationHistory {

    private String userName;
    private OperationHistoryType historyType;
    private Integer card;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public OperationHistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(OperationHistoryType historyType) {
        this.historyType = historyType;
    }

    public Integer getCard() {
        return card;
    }

    public void setCard(Integer card) {
        this.card = card;
    }

    public OperationHistory() {
    }

    public OperationHistory(String userName, OperationHistoryType historyType, Integer card) {
        this.userName = userName;
        this.historyType = historyType;
        this.card = card;
    }
}
