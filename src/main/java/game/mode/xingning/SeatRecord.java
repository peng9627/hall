package game.mode.xingning;

import java.util.ArrayList;
import java.util.List;

public class SeatRecord {
    private int userId;                         //用户名
    private String nickname;
    private String head;
    private List<Integer> initialCards;         //初始牌
    private List<Integer> cards;                //牌
    private List<Integer> pengCards = new ArrayList<>();             //碰牌
    private List<Integer> anGangCards = new ArrayList<>();           //杠的牌
    private List<Integer> mingGangCards = new ArrayList<>();         //杠的牌
    private List<Integer> chiCards = new ArrayList<>();              //吃的牌
    private int winOrLose;                      //输赢分数
    private GameResult cardResult;              //结算
    private List<GameResult> mingGangResult = new ArrayList<>();        //明杠
    private List<GameResult> anGangResult = new ArrayList<>();        //暗杠
    private int huCard;
    private int score;
    private boolean sex;                        //性别
    private String ip;
    private int seatNo;
    private int gameCount;
    private int maScore;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
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

    public int getWinOrLose() {
        return winOrLose;
    }

    public void setWinOrLose(int winOrLose) {
        this.winOrLose = winOrLose;
    }

    public GameResult getCardResult() {
        return cardResult;
    }

    public void setCardResult(GameResult cardResult) {
        this.cardResult = cardResult;
    }

    public List<GameResult> getMingGangResult() {
        return mingGangResult;
    }

    public void setMingGangResult(List<GameResult> mingGangResult) {
        this.mingGangResult = mingGangResult;
    }

    public List<GameResult> getAnGangResult() {
        return anGangResult;
    }

    public void setAnGangResult(List<GameResult> anGangResult) {
        this.anGangResult = anGangResult;
    }

    public List<Integer> getPengCards() {
        return pengCards;
    }

    public void setPengCards(List<Integer> pengCards) {
        this.pengCards = pengCards;
    }

    public List<Integer> getAnGangCards() {
        return anGangCards;
    }

    public void setAnGangCards(List<Integer> anGangCards) {
        this.anGangCards = anGangCards;
    }

    public List<Integer> getMingGangCards() {
        return mingGangCards;
    }

    public void setMingGangCards(List<Integer> mingGangCards) {
        this.mingGangCards = mingGangCards;
    }

    public List<Integer> getChiCards() {
        return chiCards;
    }

    public void setChiCards(List<Integer> chiCards) {
        this.chiCards = chiCards;
    }

    public int getHuCard() {
        return huCard;
    }

    public void setHuCard(int huCard) {
        this.huCard = huCard;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(int seatNo) {
        this.seatNo = seatNo;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getMaScore() {
        return maScore;
    }

    public void setMaScore(int maScore) {
        this.maScore = maScore;
    }
}
