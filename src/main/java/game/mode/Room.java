package game.mode;


import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class Room {

    private Double baseScore; //基础分
    private String roomNo;  //桌号
    private List<Seat> seats;//座位
    private int operationSeat;
    private List<OperationHistory> historyList;
    private List<Integer> surplusCards;//剩余的牌
    private GameStatus gameStatus;
    private List<GameResult> gameResults;

    private String lastOperation;

    private String banker;//庄家
    private int gameTimes; //游戏局数
    private int count;//人数
    private boolean dianpao;//点炮
    private Integer[] dice;//骰子

    public Double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public int getOperationSeat() {
        return operationSeat;
    }

    public void setOperationSeat(int operationSeat) {
        this.operationSeat = operationSeat;
    }

    public List<OperationHistory> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<OperationHistory> historyList) {
        this.historyList = historyList;
    }

    public List<Integer> getSurplusCards() {
        return surplusCards;
    }

    public void setSurplusCards(List<Integer> surplusCards) {
        this.surplusCards = surplusCards;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<GameResult> getGameResults() {
        return gameResults;
    }

    public void setGameResults(List<GameResult> gameResults) {
        this.gameResults = gameResults;
    }

    public String getBanker() {
        return banker;
    }

    public void setBanker(String banker) {
        this.banker = banker;
    }

    public int getGameTimes() {
        return gameTimes;
    }

    public void setGameTimes(int gameTimes) {
        this.gameTimes = gameTimes;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isDianpao() {
        return dianpao;
    }

    public void setDianpao(boolean dianpao) {
        this.dianpao = dianpao;
    }

    public Integer[] getDice() {
        return dice;
    }

    public void setDice(Integer[] dice) {
        this.dice = dice;
    }

    public Room() {
    }

    public void addSeat(User user) {
        Seat seat = new Seat();
        seat.setRobot(false);
        seat.setReady(false);
        seat.setAreaString("");
        seat.setEnd(false);
        seat.setGold(0);
        seat.setScore(0);
        seat.setSeatNo(seats.size() + 1);
        seat.setUserName(user.getUsername());
        seats.add(seat);
    }

    public Room(Double baseScore, String roomNo, String banker, int gameTimes, int count, boolean dianpao) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.banker = banker;
        this.gameTimes = gameTimes;
        this.count = count;
        this.dianpao = dianpao;
        this.seats = new ArrayList<>();
        this.historyList = new ArrayList<>();
        this.gameResults = new ArrayList<>();
        this.dice = new Integer[]{0, 0};
        this.gameStatus = GameStatus.WAITING;
        this.lastOperation = "";
    }
}
