package game.mode;

/**
 * Created by pengyi on 16-7-5.
 */
public enum ArenaType {

    DIAMONDS("钻石赛", 1),
    BILL("话费赛", 2);

    ArenaType(String name, int value) {
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
