package game.mode;

/**
 * Created by pengyi
 * Date : 16-6-30.
 */
public enum GameStatus {

    WAITING("等待开始", 0),
    READYING("准备", 1),
    PLAYING("游戏中", 2);

    private String name;
    private Integer values;

    GameStatus(String name, Integer values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValues() {
        return values;
    }

    public void setValues(Integer values) {
        this.values = values;
    }

}
