package game.mode.songjianghe;

/**
 * Created by pengyi
 * Date : 16-6-12.
 */
public enum ScoreType {

    BA_GANG("扒杠", 1),
    AN_GANG("暗杠", 2),
    DIAN_GANG("点杠", 3),
    PING_HU("平胡", 4),
    ZIMO_HU("自摸", 5),
    MENQING_HU("门清", 6),
    PENGPENG_HU("碰碰胡", 7),
    HUNYISE_HU("混一色", 8),
    QINGYISE_HU("清一色", 9),
    QIXIAODUI_HU("七小对", 10),
    HUNYAOJIU_HU("混幺九", 11),
    HAOHUAQIXIAODUI_HU("豪华七小对", 12),
    SHISANYAO_HU("十三幺", 13),
    TIAN_HU("天胡", 14),
    DI_HU("地胡", 15),
    QUANYAOJIU_HU("全幺九", 16),
    QUANFAN_HU("全番", 17),
    SHUANGHAOHUAQIXIAODUI_HU("双豪华七小对", 18),
    SANHAOHUAQIXIAODUI_HU("三豪华七小对", 19),
    SHIBALUOHAN("十八罗汉", 20),
    HAIDI("海底", 21),
    GANGBAO("杠爆", 22),
    ZHUANGYING("庄硬", 23),
    FEI("飞", 24);

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
