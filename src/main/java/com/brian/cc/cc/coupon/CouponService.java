package com.brian.cc.cc.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 用户请求（抢券）
 *    ↓
 * 接口层（API 网关 / 控制器）
 *    ↓
 * Redis 券池（Set）执行 spop 弹出券码
 *    ↓
 * 校验是否已抢（Set 判重）
 *    ↓
 * Lua 脚本原子执行：抢券 + 标记用户
 *    ↓
 * 返回抢到的券码
 *    ↓
 * 写入数据库（异步落库 / MQ）
 */
// 1. 优惠券服务类
@Service
public class CouponService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    public static String poolKeyS = "coupon:pool:";
    public static String poolKeyG = "coupon:got:";
    public static String poolKeyB = "coupon:bind:";
    public void initCouponPool(String activityId, List<String> couponIds) {
        String poolKey = poolKeyS + activityId;
        redisTemplate.delete(Arrays.asList( poolKeyS+ activityId,poolKeyG+ activityId));
        redisTemplate.opsForSet().add(poolKey, couponIds.toArray(new String[0]));
        redisTemplate.expire(poolKey, Duration.ofMinutes(30));
    }

    public String grabCoupon(String activityId, String userId) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(loadLuaScript());
        script.setResultType(String.class);

        String result = redisTemplate.execute(
                script,
                Arrays.asList(
                        poolKeyS + activityId,
                        poolKeyG + activityId,
                        poolKeyB + userId
                ),
                userId
        );

        return result; // 返回抢到的优惠券ID，或 null
    }

    /**
     * 用户是否已抢过？
     *     ├── 是 → return nil
     *     └── 否 →
     *         券池还有券吗？
     *            ├── 是 → 抢券 → 标记用户 → 记录绑定关系 → return 券码
     *            └── 否 → return nil
     * @return
     */
    private String loadLuaScript() {

        return """
            if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
              return nil
            end
            local couponId = redis.call('spop', KEYS[1])
            if couponId then
              redis.call('sadd', KEYS[2], ARGV[1])
              redis.call('hset', KEYS[3], ARGV[1], couponId)
              return couponId
            else
              return nil
            end
        """;
    }
}