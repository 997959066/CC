package com.brian.cc.cc.packet;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 1. 红包实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedPacket {
    private String redPacketId;
    private List<Integer> amounts; // 单位：分
}
