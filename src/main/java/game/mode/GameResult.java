package game.mode;

import java.util.List;

/**
 * Author pengyi
 * Date 17-3-21.
 */
public class GameResult {

    private int winSeat;
    private ScoreType scoreType;
    private List<Integer> loseSeats;
    private double score;

    public int getWinSeat() {
        return winSeat;
    }

    public void setWinSeat(int winSeat) {
        this.winSeat = winSeat;
    }

    public ScoreType getScoreType() {
        return scoreType;
    }

    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    public List<Integer> getLoseSeats() {
        return loseSeats;
    }

    public void setLoseSeats(List<Integer> loseSeats) {
        this.loseSeats = loseSeats;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public GameResult() {
    }

    public GameResult(int winSeat, ScoreType scoreType, List<Integer> loseSeats, double score) {
        this.winSeat = winSeat;
        this.scoreType = scoreType;
        this.loseSeats = loseSeats;
        this.score = score;
    }
}
