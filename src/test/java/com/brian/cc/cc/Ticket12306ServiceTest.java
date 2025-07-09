package com.brian.cc.cc;

import com.brian.cc.cc.ticket12306.TicketService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Ticket12306ServiceTest {


    @Autowired
    private TicketService ticketService;

    private final String trainId = "G123";

    @BeforeEach
    public void init() {
        // 初始化 10 张座位票
        ticketService.initTickets(trainId, 10);
    }

    @Test
    public void testGrabTicket() {
        int successCount = 0;

        for (int i = 0; i < 15; i++) {
            String userId = "user-" + i;
            String seat = ticketService.grabTicket(trainId, userId);
            if (seat != null) {
                System.out.println(userId + " 抢到座位：" + seat);
                successCount++;
            } else {
                System.out.println(userId + " 抢票失败！");
            }
        }

        System.out.println("抢票成功人数：" + successCount);
        Assertions.assertEquals(10, successCount, "应该只有10个用户能成功抢票");
    }
}
