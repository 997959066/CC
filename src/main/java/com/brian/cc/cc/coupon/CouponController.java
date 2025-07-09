package com.brian.cc.cc.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 2. 控制器类
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/init")
    public String initPool(@RequestParam String activityId, @RequestBody List<String> couponIds) {
        couponService.initCouponPool(activityId, couponIds);
        return "券池初始化成功";
    }

    @GetMapping("/grab")
    public String grab(@RequestParam String activityId, @RequestParam String userId) {
        String couponId = couponService.grabCoupon(activityId, userId);
        if (couponId != null) {
            return userId + " 抢到优惠券：" + couponId;
        } else {
            return userId + " 抢券失败";
        }
    }
}