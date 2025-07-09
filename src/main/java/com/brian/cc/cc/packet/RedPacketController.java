package com.brian.cc.cc.packet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 3. 控制器类
@RestController
@RequestMapping("/red-packet")
public class RedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    @PostMapping("/create")
    public String create(@RequestBody RedPacket packet) {
        redPacketService.preGenerateRedPacket(packet);
        return "红包创建成功";
    }

    @GetMapping("/grab")
    public String grab(@RequestParam String redPacketId, @RequestParam String userId) {
        Integer amount = redPacketService.grabRedPacket(redPacketId, userId);
        if (amount != null) {
            return userId + " 抢到红包 " + (amount / 100.0) + " 元";
        } else {
            return userId + " 抢红包失败";
        }
    }
}