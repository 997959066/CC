package com.brian.cc.cc;

import com.brian.cc.cc.packet.RedPacket;
import com.brian.cc.cc.packet.RedPacketService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class RedPacketServiceTest {

    @Autowired
    private RedPacketService redPacketService;

    private final String redPacketId = "test-rp-001";

    @BeforeEach
    public void setup() {
        // 创建测试红包，金额总共10元，分成10个包
        List<Integer> amounts = Arrays.asList(100, 100, 100, 100, 100, 100, 100, 100, 100, 100); // 单位：分
        RedPacket packet = new RedPacket(redPacketId, amounts);
        redPacketService.preGenerateRedPacket(packet);
    }

    @Test
    public void testGrabRedPacket() {
        int successCount = 0;

        for (int i = 0; i < 15; i++) { // 模拟15个人抢10个包
            String userId = "user-" + i;
            Integer amount = redPacketService.grabRedPacket(redPacketId, userId);
            if (amount != null) {
                System.out.println(userId + " 抢到 " + amount / 100.0 + " 元");
                successCount++;
            } else {
                System.out.println(userId + " 抢红包失败！");
            }
        }

        System.out.println("抢到红包的人数：" + successCount);
        Assertions.assertEquals(10, successCount); // 断言：只能有10人抢到
    }
}
