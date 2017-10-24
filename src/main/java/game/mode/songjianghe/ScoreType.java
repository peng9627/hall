package game.mode.songjianghe;

/**
 * Created by pengyi
 * Date : 16-6-12.
 */
public enum ScoreType {

    BA_GANG("扒杠", 1),
    AN_GANG("暗杠", 2),
    DIAN_GANG("点杠", 3),
    ANTING("暗听", 4),
    PENGPENGHU("碰碰胡", 5),
    LIMEN("立门", 6),
    HUNYISE("混一色", 7),
    SHOUBAYI("手把一", 8),
    GANGHOUKAI("杠后开", 9),
    QINGYISE("清一色", 10),
    YIBANGAO("一般高", 11),
    SIGUIYI("四归一", 12),
    SHISANYAO("十三幺", 13),
    SANJIAQING("三家清", 14),
    SIJIAQING("四家清", 15),
    QIDUI("七对", 16),
    DIANPAO("点炮", 17),
    ZIMO("自摸", 18),
    ZUOZHUANG("坐庄", 19),
    XUANFENGGANG("旋风杠", 20),
    GANGHOUPAO("杠后炮", 21);

    private String name;
    private Integer values;

    ScoreType(String name, Integer values) {
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
