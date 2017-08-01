package game.mode;

/**
 * Created by pengyi
 * Date : 16-6-12.
 */
//////////////////////- 四川麻将胡法 //////////////////-
// CheckPh_SC()   //平胡    普通的四个搭子一对将
// CheckDdz_SC()  //大对子  四个搭子均为三张一样的牌
// CheckQys_SC()  //清一色  胡牌时只有一色牌
// CheckAqd_SC()  //七对  特殊胡牌类型，不遵循四个搭子一对将，胡牌时为7个对子
// CheckQdd_SC()  //清大对  清一色+大对子
// CheckLqd_SC()  //龙七对  暗七对的改进，七对中有两对（或更多）相同，可视作带根的暗七对。
// CheckQqd_SC()  //清七对  清一色+暗七对
// CheckQlqd_SC() //清龙七对  清一色+龙七对
public enum ScoreType {

    BA_GANG("扒杠", 1),
    AN_GANG("暗杠", 2),
    DIAN_GANG("点杠", 3),
    PING_HU("平胡", 4),
    DADUI_HU("大对", 5),
    JINGOUDIAO_HU("金勾掉", 6),
    QIDUI_HU("七对", 7),
    LONGQIDUI_HU("笼七对", 8),
    QINGYISE_HU("清一色", 9),
    QINGDADUI_HU("清大对", 10),
    QINGJINGOUDIAO_HU("清金勾掉", 11),
    QINGQIDUI_HU("清七对", 12),
    QINGLONGQIDUI_HU("清笼七对", 13);

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
