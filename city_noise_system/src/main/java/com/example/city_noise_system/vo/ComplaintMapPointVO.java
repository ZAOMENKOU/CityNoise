package com.example.city_noise_system.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投诉地图点视图对象
 * 专门用于地图展示，只包含地图需要的核心信息
 */
@Data
public class ComplaintMapPointVO {
    // 投诉ID
    private Long id;

    // 投诉标题
    private String title;

    // 投诉状态，用于在地图上用不同颜色标记
    private String status;

    // 经度
    private BigDecimal longitude;

    // 纬度
    private BigDecimal latitude;

    // 行政区
    private String district;

    // 距离，单位米
    private Integer distance;

    // 创建时间
    private LocalDateTime createTime;

    // 噪音类型
    private String noiseType;
}