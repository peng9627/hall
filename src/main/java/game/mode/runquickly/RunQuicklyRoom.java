package game.mode.runquickly;


import game.mode.GameStatus;
import game.mode.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class RunQuicklyRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
    private int roomOwner;
    private List<Seat> seats = new ArrayList<>();//座位
    private GameStatus gameStatus;
    private List<Integer> seatNos;

    private int gameTimes; //游戏局数
    private int count;//人数
    private int multiple;
    private int gameCount;
    private int gameRules;

    public int getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(int baseScore) {
        this.baseScore = baseScore;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<Integer> getSeatNos() {
        return seatNos;
    }

    public void setSeatNos(List<Integer> seatNos) {
        this.seatNos = seatNos;
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

    public int getMultiple() {
        return multiple;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getGameRules() {
        return gameRules;
    }

    public void setGameRules(int gameRules) {
        this.gameRules = gameRules;
    }

    public RunQuicklyRoom() {
    }

    public RunQuicklyRoom(int baseScore, String roomNo, int gameTimes, int count, int gameRules, int roomOwner) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.roomOwner = roomOwner;
        this.gameTimes = gameTimes;
        this.count = count;
        this.gameRules = gameRules;
        this.gameStatus = GameStatus.WAITING;
        this.multiple = 1;
        seatNos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seatNos.add(i + 1);
        }
    }

    public void addSeats(List<User> users) {
        for (User user : users) {
            Seat seat = new Seat();
            seat.setAreaString("");
            seat.setHead(user.getHead());
            seat.setNickname(user.getNickname());
            seat.setSex(user.getSex().equals("MAN"));
            seat.setScore(1000);
            seat.setSeatNo(seatNos.get(0));
            seat.setCanPlay(true);
            seatNos.remove(0);
            seat.setUserId(user.getUserId());
            seats.add(seat);
        }
    }

}
