package game.mode.ruijin;


import game.mode.GameStatus;
import game.mode.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class RuijinMahjongRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
    private List<Seat> seats = new ArrayList<>();//座位
    private List<Integer> seatNos;
    private GameStatus gameStatus;

    private int banker;//庄家
    private int gameTimes; //游戏局数
    private int count;//人数
    private boolean dianpao;//点炮
    private int roomOwner;
    private int zhuangxian;

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

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
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

    public boolean isDianpao() {
        return dianpao;
    }

    public void setDianpao(boolean dianpao) {
        this.dianpao = dianpao;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public int getZhuangxian() {
        return zhuangxian;
    }

    public void setZhuangxian(int zhuangxian) {
        this.zhuangxian = zhuangxian;
    }

    public RuijinMahjongRoom() {
    }

    public RuijinMahjongRoom(int baseScore, String roomNo, int banker, int gameTimes, int count, boolean dianpao, int zhuangxian) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.banker = banker;
        this.roomOwner = banker;
        this.gameTimes = gameTimes;
        this.count = count;
        this.dianpao = dianpao;
        this.zhuangxian = zhuangxian;
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
