package game.mode;


import java.util.List;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class SangongRoom {

    private int baseScore; //基础分
    private String roomNo;  //桌号
    private GameStatus gameStatus;
    private List<Integer> seatNos;
    private int gameTimes; //游戏局数
    private int count;//人数
    private int grab;//庄家

    public SangongRoom(int baseScore, String roomNo, int gameTimes, int count) {
        this.baseScore = baseScore;
        this.roomNo = roomNo;
        this.gameTimes = gameTimes;
        this.count = count;
        this.gameStatus = GameStatus.WAITING;
        for (int i = 0; i < count; i++) {
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

    public int getGrab() {
        return grab;
    }

    public void setGrab(int grab) {
        this.grab = grab;
    }
}
