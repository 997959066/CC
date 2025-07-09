package com.brian.cc.cc.ticket12306;

// 🚄 抢票系统 DEMO：Spring Boot + Redis 实现（简化版，非 12306 官方 API 模拟）

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

// 1. TicketService.java
@Service
public class TicketService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 初始化某车次的余票数据（如 20 张票）
    public void initTickets(String trainId, int totalTickets) {
        String key = "ticket:pool:" + trainId;
        redisTemplate.delete(key);
        for (int i = 1; i <= totalTickets; i++) {
            redisTemplate.opsForList().rightPush(key, "SEAT-" + i);
        }
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    // 抢票逻辑（原子操作）
    public String grabTicket(String trainId, String userId) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(loadLuaScript());
        script.setResultType(String.class);

        String result = redisTemplate.execute(
                script,
                Arrays.asList(
                        "ticket:pool:" + trainId,         // KEYS[1]
                        "ticket:users:" + trainId          // KEYS[2]
                ),
                userId                                 // ARGV[1]
        );

        return result;
    }

    /**
     * 抢票核心流程（Lua 脚本执行）
     *
     * 用户是否已抢过？
     * ├── 是 → return nil （已抢过）
     * └── 否 →
     *     票池还有票吗？
     *     ├── 是 →
     *     │   ├── 弹出座位号（lpop）
     *     │   ├── 标记用户为已抢（Set）
     *     │   └── 返回座位号 → return seat
     *     └── 否 → return nil （票已抢完）
     */

    private String loadLuaScript() {
        return """
            if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
              return nil
            end
            local seat = redis.call('lpop', KEYS[1])
            if seat then
              redis.call('sadd', KEYS[2], ARGV[1])
              return seat
            else
              return nil
            end
        """;
    }
}