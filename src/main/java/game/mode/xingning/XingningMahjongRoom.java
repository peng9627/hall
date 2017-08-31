package game.mode.xingning;


import game.mode.GameStatus;
import game.mode.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class XingningMahjongRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
    private List<Integer> seatNos;
    private GameStatus gameStatus;
    private List<Seat> seats = new ArrayList<>();//座位

    private int banker;//庄家
    private int gameTimes; //游戏局数
    private int count;//人数
    private int ghost;//1.红中做鬼，2.无鬼，3.翻鬼，4.无鬼加倍
    private int gameRules;////游戏规则  高位到低位顺序（鸡胡，门清，天地和，幺九，全番，十三幺，对对胡，十八罗汉，七小对，清一色，混一色，海底捞，杠爆全包，庄硬）

    private int initMaCount;

    private int roomOwner;

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

    public int getGhost() {
        return ghost;
    }

    public void setGhost(int ghost) {
        this.ghost = ghost;
    }

    public int getGameRules() {
        return gameRules;
    }

    public void setGameRules(int gameRules) {
        this.gameRules = gameRules;
    }

    public int getInitMaCount() {
        return initMaCount;
    }

    public void setInitMaCount(int initMaCount) {
        this.initMaCount = initMaCount;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public XingningMahjongRoom() {
    }

    public XingningMahjongRoom(int baseScore, String roomNo, int banker, int gameTimes, int count, int initMaCount, int ghost, int gameRules) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.banker = banker;
        this.roomOwner = banker;
        this.gameTimes = gameTimes;
        this.count = count;
        this.initMaCount = initMaCount;
        this.ghost = ghost;
        this.gameRules = gameRules;
        this.gameStatus = GameStatus.WAITING;
        seatNos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seatNos.add(i + 1);
        }
    }

    public void addSeats(List<User> users) {
        for (User user : users) {
            Seat seat = new Seat();
            seat.setReady(false);
            seat.setAreaString(user.getArea());
            seat.setHead(user.getHead());
            seat.setNickname(user.getNickname());
            seat.setSex(user.getSex().equals("MAN"));
            seat.setScore(0);
            seat.setSeatNo(seatNos.get(0));
            seatNos.remove(0);
            seat.setUserId(user.getUserId());
            seats.add(seat);
        }
    }
}
