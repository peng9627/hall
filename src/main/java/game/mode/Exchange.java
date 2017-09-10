package game.mode;

import java.util.Date;

/**
 * Created by pengyi
 * Date : 17-9-1.
 * desc:
 */
public class Exchange {

    private Date createDate;
    private String goodsName;
    private Integer price;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
