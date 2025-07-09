package com.brian.cc.cc.packet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

// 2. 红包服务类
@Service
public class RedPacketService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void preGenerateRedPacket(RedPacket redPacket) {
        String listKey = "red:packet:amounts:" + redPacket.getRedPacketId();
        String userKey = "red:packet:users:" + redPacket.getRedPacketId();

        redisTemplate.delete(listKey);
        redisTemplate.delete(userKey);

        for (Integer amount : redPacket.getAmounts()) {
            redisTemplate.opsForList().rightPush(listKey, String.valueOf(amount));
        }

        redisTemplate.expire(listKey, Duration.ofMinutes(10));
        redisTemplate.expire(userKey, Duration.ofMinutes(10));
    }

    public Integer grabRedPacket(String redPacketId, String userId) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(loadLuaScript());
        script.setResultType(String.class);

        String result = redisTemplate.execute(
                script,
                Arrays.asList(
                        "red:packet:amounts:" + redPacketId,
                        "red:packet:users:" + redPacketId
                ),
                userId
        );

        if (result != null) {
            return Integer.parseInt(result);
        } else {
            return null; // 没抢到
        }
    }

    private String loadLuaScript() {
        return """
            if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
              return nil
            end
            local amount = redis.call('rpop', KEYS[1])
            if amount then
              redis.call('sadd', KEYS[2], ARGV[1])
              return amount
            else
              return nil
            end
        """;
    }
}
