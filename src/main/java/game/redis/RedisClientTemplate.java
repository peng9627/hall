package game.redis;

import game.utils.CoreDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.*;

/**
 * Author pengyi
 * Date 16-12-20.
 */
public class RedisClientTemplate {

    private static final Logger log = LoggerFactory.getLogger(RedisClientTemplate.class);

    private final ShardedJedisPool shardedJedisPool;

    public RedisClientTemplate(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    public void disconnect() {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        shardedJedis.disconnect();
    }

    /**
     * 设置单个值
     *
     * @param key   键
     * @param value 值
     * @return 结果
     */
    public String set(String key, String value, int timeout) {
        log.info("redis 添加" + key);
        String result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.set(key, value);
            if (0 != timeout) {
                shardedJedis.expire(key, timeout);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 获取单个值
     *
     * @param key 键
     * @return 结果
     */
    public String get(String key) {
        String result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }

        try {
            result = shardedJedis.get(key);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 获取单个值
     *
     * @param likeKey 键
     * @return 结果
     */
    public List<String> gets(String likeKey) {
        List<String> result = new ArrayList<>();
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }

        try {
            Collection<Jedis> jedisC = shardedJedis.getAllShards();
            Iterator<Jedis> iter = jedisC.iterator();
            while (iter.hasNext()) {
                Jedis _jedis = iter.next();
                Set<String> keys = _jedis.keys(likeKey + "*");
                keys.forEach(s -> result.add(_jedis.get(s)));
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public Boolean exists(String key) {
        Boolean result = false;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return false;
        }
        try {
            result = shardedJedis.exists(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public String type(String key) {
        String result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.type(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 在某段时间后实现
     *
     * @param key     键
     * @param seconds 时间
     * @return 返回
     */
    public Long expire(String key, int seconds) {
        Long result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.expire(key, seconds);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public String getSet(String key, String value) {
        String result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.getSet(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public Long append(String key, String value) {
        Long result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.append(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public String substr(String key, int start, int end) {
        String result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.substr(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    public Long del(String key) {
        log.info("redis 删除" + key);
        Long result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.del(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        log.info("redis 删除" + key + "成功");
        return result;
    }

    public List<String> sort(String key) {
        List<String> result = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            result = shardedJedis.sort(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }


    /**
     * 获取分布式锁
     *
     * @param lockedKey 建议业务名称+业务主键
     * @param timeout   获取分布式锁的超时时间(毫秒)
     * @return true：获取锁成功，fasle:获取锁失败
     */
    public boolean lock(String lockedKey, long timeout) {
        long nano = System.nanoTime();
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        if (shardedJedis == null) {
            return false;
        }
        Long del = null;
        try {
            long timeoutNanos = timeout * 1000000L;
            while ((System.nanoTime() - nano) < timeoutNanos) {
                if (shardedJedis.setnx(lockedKey, CoreDateUtils.formatDate(new Date(), "yyyyMMddHHmmssSSS")) == 1) {
                    shardedJedis.expire(lockedKey, 5);
                    log.info("lock-->lockedKey=‘{}’", lockedKey);
                    return true;
                }
                // 短暂休眠，nano避免出现活锁
                try {
                    Thread.sleep(2, new Random().nextInt(500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return false;
    }

    /**
     * 获取分布式锁
     *
     * @param lockedKey 建议业务名称+业务主键
     * @return true：获取锁成功，fasle:获取锁失败
     */
    public boolean lock(String lockedKey) {
        return lock(lockedKey, 6);
    }

    /**
     * 获取调度锁
     * 注意点：1.无需释放锁；2.任务调度周期>5分钟  && 任务处理耗时 > 1秒
     *
     * @param lockedKey 调度锁的key
     * @return true：获取锁成功，fasle:获取锁失败
     * @desc instanceMaxDiffTime：集群下各实例所在应用服务器的最大时间差
     * timeout：超时时间，默认为1秒
     * tasktime：任务执行所需时间
     * expireTime：锁有效时间，默认为5分钟
     * scheduletime：调度周期
     * 调度锁有效条件： 0<=instanceMaxDiffTime<timeout<tasktime<expireTime<scheduletime
     * 此方法默认不调用解锁下,锁的有效条件：1秒<tasktime<5分钟<scheduletime
     */
    public boolean scheduleLock(String lockedKey) {
        return lock(lockedKey, 300);
    }

    /**
     * 释放分布式锁
     *
     * @param lockedKey 建议业务名称+业务主键
     */
    public void unlock(String lockedKey) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return;
        }
        Long del = null;
        try {
            del = shardedJedis.del(lockedKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }

        log.info("unlock-->lockedKey=‘{}’", lockedKey);
    }
}
