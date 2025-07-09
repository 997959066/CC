package com.brian.cc.cc.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

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