package com.brian.cc.cc.ticket12306;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 2. TicketController.java
@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/init")
    public String init(@RequestParam String trainId, @RequestParam int total) {
        ticketService.initTickets(trainId, total);
        return "已初始化余票";
    }

    @GetMapping("/grab")
    public String grab(@RequestParam String trainId, @RequestParam String userId) {
        String seat = ticketService.grabTicket(trainId, userId);
        if (seat != null) {
            return userId + " 抢到座位：" + seat;
        } else {
            return userId + " 抢票失败";
        }
    }
}