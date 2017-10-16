package game.mode.sangong;


import game.mode.GameStatus;
import game.mode.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class SangongRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
    private GameStatus gameStatus;
    private List<Seat> seats = new ArrayList<>();//座位
    private List<Integer> seatNos = new ArrayList<>();
    private int gameTimes; //游戏局数
    private int grab;//庄家
    private int bankerWay;//庄家方式
    private int roomOwner;
    private int payType;//支付方式
    private int count;//人数

    public SangongRoom() {
    }

    public SangongRoom(int baseScore, String roomNo, int gameTimes, int bankerWay, int userId, int payType, int count) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.gameTimes = gameTimes;
        this.bankerWay = bankerWay;
        this.gameStatus = GameStatus.WAITING;
        this.roomOwner = userId;
        this.payType = payType;
        this.count = count;
        if (2 == bankerWay) {
            this.grab = userId;
        }
        this.seatNos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            seatNos.add(i + 1);
        }
    }

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

    public int getGrab() {
        return grab;
    }

    public void setGrab(int grab) {
        this.grab = grab;
    }

    public int getBankerWay() {
        return bankerWay;
    }

    public void setBankerWay(int bankerWay) {
        this.bankerWay = bankerWay;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addSeats(List<User> users) {
        for (User user : users) {
            Seat seat = new Seat();
            seat.setAreaString(user.getArea());
            seat.setScore(1000);
            seat.setIp(user.getLastLoginIp());
            seat.setGameCount(user.getGameCount());
            seat.setSeatNo(seats.size() + 1);
            seat.setUserId(user.getUserId());
            seat.setHead(user.getHead());
            seat.setNickname(user.getNickname());
            seat.setSex(user.getSex().equals("1"));
            seats.add(seat);
        }
    }
}
