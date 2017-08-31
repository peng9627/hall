package game.mode.xingning;

import java.util.List;

public class Record {

    private Integer jiabao;
    private Integer banker;
    private Integer[] dice;//骰子
    private List<OperationHistory> historyList;
    private List<SeatRecord> seatRecordList;//座位战绩信息

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
}
