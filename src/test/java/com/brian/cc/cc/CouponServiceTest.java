package com.brian.cc.cc;

import com.brian.cc.cc.coupon.CouponService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    private final String activityId = "test-activity-001";

    @BeforeEach
    public void setup() {
        // 初始化 5 张优惠券
        List<String> couponIds = Arrays.asList("COUPON-1", "COUPON-2", "COUPON-3", "COUPON-4", "COUPON-5");
        couponService.initCouponPool(activityId, couponIds);
    }

    @Test
    public void testGrabCoupons() {
        int successCount = 0;

        for (int i = 0; i < 10; i++) {
            String userId = "user-" + i;
            String coupon = couponService.grabCoupon(activityId, userId);

            if (coupon != null) {
                successCount++;
                System.out.println(userId + " 抢到优惠券：" + coupon);
            } else {
                System.out.println(userId + " 抢券失败！");
            }
        }

        System.out.println("共抢到优惠券人数：" + successCount);
        Assertions.assertEquals(5, successCount); // 总共只有 5 张券
    }
}
