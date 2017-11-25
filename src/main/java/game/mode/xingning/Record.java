package game.mode.xingning;

import java.util.Date;
import java.util.List;

public class Record {

    private Integer jiabao;
    private Integer banker;
    private Integer[] dice;//骰子
    private List<OperationHistory> historyList;
    private List<SeatRecord> seatRecordList;//座位战绩信息
    private Date startDate;
    private int gameCount;
    private int changeDice;

    public Integer getJiabao() {
        return jiabao;
    }

    public void setJiabao(Integer jiabao) {
        this.jiabao = jiabao;
    }

    public Integer getBanker() {
        return banker;
    }

    public void setBanker(Integer banker) {
        this.banker = banker;
    }

    public Integer[] getDice() {
        return dice;
    }

    public void setDice(Integer[] dice) {
        this.dice = dice;
    }

    public List<OperationHistory> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<OperationHistory> historyList) {
        this.historyList = historyList;
    }

    public List<SeatRecord> getSeatRecordList() {
        return seatRecordList;
    }

    public void setSeatRecordList(List<SeatRecord> seatRecordList) {
        this.seatRecordList = seatRecordList;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getChangeDice() {
        return changeDice;
    }

    public void setChangeDice(int changeDice) {
        this.changeDice = changeDice;
    }
}
