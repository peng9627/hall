package game.mode.xingning;


import game.mode.GameStatus;
import game.mode.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class RongchangMahjongRoom {

    private String roomNo;  //桌号
    private List<Integer> seatNos;
    private GameStatus gameStatus;
    private List<Seat> seats = new ArrayList<>();//座位

    private int banker;//庄家
    private int gameTimes; //游戏局数
    private int count;//人数
    private int gameRules;////游戏规则  高位到低位顺序（换三张，后四必胡，GPS，ip一致不能进房间）
    private boolean aa;//AA支付

    private int roomOwner;

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public List<Integer> getSeatNos() {
        return seatNos;
    }

    public void setSeatNos(List<Integer> seatNos) {
        this.seatNos = seatNos;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public int getBanker() {
        return banker;
    }

    public void setBanker(int banker) {
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

    public int getGameRules() {
        return gameRules;
    }

    public void setGameRules(int gameRules) {
        this.gameRules = gameRules;
    }

    public boolean isAa() {
        return aa;
    }

    public void setAa(boolean aa) {
        this.aa = aa;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public RongchangMahjongRoom() {
    }

    public RongchangMahjongRoom(String roomNo, int banker, int gameTimes, int count, int gameRules, boolean aa) {
        this.roomNo = roomNo;
        this.banker = banker;
        this.roomOwner = banker;
        this.gameTimes = gameTimes;
        this.count = count;
        this.gameRules = gameRules;
        this.aa = aa;
        this.gameStatus = GameStatus.WAITING;
        seatNos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seatNos.add(i + 1);
        }
    }

    public void addSeats(List<User> users) {
        for (User user : users) {
            Seat seat = new Seat();
            seat.setAreaString(user.getArea());
            seat.setHead(user.getHead());
            seat.setNickname(user.getNickname());
            seat.setSex(user.getSex().equals("1"));
            seat.setScore(1000);
            seat.setIp(user.getLastLoginIp());
            seat.setGameCount(user.getGameCount());
            seat.setSeatNo(seatNos.get(0));
            seatNos.remove(0);
            seat.setUserId(user.getUserId());
            seats.add(seat);
        }
    }
}
