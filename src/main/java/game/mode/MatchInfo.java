package game.mode;

import java.util.List;

/**
 * Created by pengyi
 * Date : 17-9-1.
 * desc:
 */
public class MatchInfo {

    private int status;
    private List<Integer> rooms;
    private Arena arena;
    private List<MatchUser> matchUsers;
    private boolean start;
    private int matchEliminateScore;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Integer> getRooms() {
        return rooms;
    }

    public void setRooms(List<Integer> rooms) {
        this.rooms = rooms;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public List<MatchUser> getMatchUsers() {
        return matchUsers;
    }

    public void setMatchUsers(List<MatchUser> matchUsers) {
        this.matchUsers = matchUsers;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public int getMatchEliminateScore() {
        return matchEliminateScore;
    }

    public void setMatchEliminateScore(int matchEliminateScore) {
        this.matchEliminateScore = matchEliminateScore;
    }
}
