package game.mode.xingning;

import java.util.List;

/**
 * Author pengyi
 * Date 17-3-21.
 */
public class GameResult {

    private List<ScoreType> scoreTypes;
    private Integer card;
    private int score;

    public GameResult() {
    }

    public GameResult(List<ScoreType> scoreTypes, Integer card, int score) {
        this.scoreTypes = scoreTypes;
        this.card = card;
        this.score = score;
    }

    public List<ScoreType> getScoreTypes() {
        return scoreTypes;
    }

    public void setScoreTypes(List<ScoreType> scoreTypes) {
        this.scoreTypes = scoreTypes;
    }

    public Integer getCard() {
        return card;
    }

    public void setCard(Integer card) {
        this.card = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
