package game.mode;


import java.util.ArrayList;
import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class RuijinMahjongRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
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
}
