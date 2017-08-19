package game.mode;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class Seat {

    private int seatNo;                         //座位号
    private int userId;                         //用户名
    private int gold;                           //金币
    private List<Integer> initialCards = new ArrayList<>();         //初始牌
    private List<Integer> cards = new ArrayList<>();                //牌
    private List<Integer> pengCards = new ArrayList<>();            //碰牌
    private List<Integer> gangCards = new ArrayList<>();            //杠的牌
    private List<Integer> chiCards = new ArrayList<>();             //吃的牌
    private List<Integer> playedCards = new ArrayList<>();          //出牌
    private int score;                          //输赢分数
    private String areaString;                  //地区
    private boolean isRobot;                    //是否托管
    private int operation;                      //标识，0.未操作，1.胡，2.杠，3.碰，4.过
    private boolean ready;                      //准备
    private boolean completed;                  //就绪
    private GameResult cardResult;              //结算
    private List<GameResult> gangResult = new ArrayList<>();        //杠

    public int getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(int seatNo) {
        this.seatNo = seatNo;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public List<Integer> getInitialCards() {
        return initialCards;
    }

    public void setInitialCards(List<Integer> initialCards) {
        this.initialCards = initialCards;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public List<Integer> getPengCards() {
        return pengCards;
    }

    public void setPengCards(List<Integer> pengCards) {
        this.pengCards = pengCards;
    }

    public List<Integer> getGangCards() {
        return gangCards;
    }

    public void setGangCards(List<Integer> gangCards) {
        this.gangCards = gangCards;
    }

    public List<Integer> getChiCards() {
        return chiCards;
    }

    public void setChiCards(List<Integer> chiCards) {
        this.chiCards = chiCards;
    }

    public List<Integer> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(List<Integer> playedCards) {
        this.playedCards = playedCards;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public GameResult getCardResult() {
        return cardResult;
    }

    public void setCardResult(GameResult cardResult) {
        this.cardResult = cardResult;
    }

    public List<GameResult> getGangResult() {
        return gangResult;
    }

    public void setGangResult(List<GameResult> gangResult) {
        this.gangResult = gangResult;
    }

}
