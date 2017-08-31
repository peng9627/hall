package game.mode;

/**
 * Created by pengyi on 16-7-5.
 */
public enum GameType {

    XINGNING_MAHJONG("兴宁麻将", 1),
    RUN_QUICKLY("跑得快", 2),
    RUIJIN_MAHJONG("瑞金麻将", 3),
    SANGONG("三公", 4);

    GameType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private int value;

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

}
