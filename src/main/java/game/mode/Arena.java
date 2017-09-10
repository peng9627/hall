package game.mode;

import java.util.Date;

/**
 * Created by pengyi
 * Date : 17-8-28.
 * desc:
 */
public class Arena {

    private String id;
    private Integer version;
    private Date createDate;

    private GameType gameType;              //游戏类型
    private Integer arenaType;            //竞技类型
    private String name;                    //竞技名
    private Integer count;                  //人数
    private Integer entryFee;               //报名费
    private Integer reward;                 //奖励

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Integer getArenaType() {
        return arenaType;
    }

    public void setArenaType(Integer arenaType) {
        this.arenaType = arenaType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(Integer entryFee) {
        this.entryFee = entryFee;
    }

    public Integer getReward() {
        return reward;
    }

    public void setReward(Integer reward) {
        this.reward = reward;
    }
}
