package game.mode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pengyi on 2016/4/19.
 */
public class User {

    private String id;
    private int version;
    private Date createDate;

    private Integer userId;             //用户id
    private String nickname;            //网名
    private String head;                //头像
    private String agent;               //终端
    private Integer money;              //房卡
    private String sex;
    private String weChatNo;            //微信号
    private String registerIp;          //注册ip
    private String lastLoginIp;         //上次登陆ip
    private String area;                //地方
    private Integer gameCount;             //游戏局数
    private Integer todayGameCount;     //今日游戏次数
    private Date lastLoginDate;         //上次登陆时间
    private Boolean status;             //状态
    private Integer integral;           //积分
    private Integer dianPao;
    private Integer zimo;
    private Integer parentId;
    private BigDecimal reward;
    private int spreadCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getWeChatNo() {
        return weChatNo;
    }

    public void setWeChatNo(String weChatNo) {
        this.weChatNo = weChatNo;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getGameCount() {
        return gameCount;
    }

    public void setGameCount(Integer gameCount) {
        this.gameCount = gameCount;
    }

    public Integer getTodayGameCount() {
        return todayGameCount;
    }

    public void setTodayGameCount(Integer todayGameCount) {
        this.todayGameCount = todayGameCount;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public Integer getDianPao() {
        return dianPao;
    }

    public void setDianPao(Integer dianPao) {
        this.dianPao = dianPao;
    }

    public Integer getZimo() {
        return zimo;
    }

    public void setZimo(Integer zimo) {
        this.zimo = zimo;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public int getSpreadCount() {
        return spreadCount;
    }

    public void setSpreadCount(int spreadCount) {
        this.spreadCount = spreadCount;
    }
}
