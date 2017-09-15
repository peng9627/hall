package game.mode.xingning;

/**
 * Created by pengyi
 * Date : 16-6-12.
 */
public enum OperationHistoryType {

    GET_CARD("摸牌", 1),
    PLAY_CARD("出牌", 2),
    PENG("碰", 3),
    AN_GANG("暗杠", 4),
    DIAN_GANG("点杠", 5),
    BA_GANG("扒杠", 6),
    HU("胡", 7),
    CHI("吃", 8);

    private String name;
    private Integer values;

    OperationHistoryType(String name, Integer values) {
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
