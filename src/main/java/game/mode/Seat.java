package game.mode;

import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class Seat {

    private int seatNo;                         //座位号
    private String userName;                    //用户名
    private float gold;                         //金币
    private List<Integer> cards;                //牌
    private List<Integer> invertedCards;        //碰或杠的牌
    private List<Integer> invertedIndex;        //碰或杠下标 自己为扒杠，0为暗杠
    private List<Integer> playedCards;          //出牌
    private boolean end;                        //是否结束
    private float score;                        //输赢分数
    private String areaString;                  //地区
    private boolean isRobot;                    //是否托管
    private int operation;                      //标识，0.未操作，1.胡，2.杠，3.碰，4.过
    private boolean ready;                      //准备

    public int getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(int seatNo) {
        this.seatNo = seatNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getGold() {
        return gold;
    }

    public void setGold(float gold) {
        this.gold = gold;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public List<Integer> getInvertedCards() {
        return invertedCards;
    }

    public void setInvertedCards(List<Integer> invertedCards) {
        this.invertedCards = invertedCards;
    }

    public List<Integer> getInvertedIndex() {
        return invertedIndex;
    }

    public void setInvertedIndex(List<Integer> invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public List<Integer> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(List<Integer> playedCards) {
        this.playedCards = playedCards;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getAreaString() {
        return areaString;
    }

    public void setAreaString(String areaString) {
        this.areaString = areaString;
    }

    public boolean isRobot() {
        return isRobot;
    }

    public void setRobot(boolean robot) {
        isRobot = robot;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
