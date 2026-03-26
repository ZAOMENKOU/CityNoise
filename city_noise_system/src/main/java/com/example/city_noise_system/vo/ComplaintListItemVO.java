package com.example.city_noise_system.vo;

import lombok.Data;
import java.util.Date;

/**
 * 投诉列表项视图对象
 * 用于列表页面展示，只包含核心信息，不包含详情
 */
@Data
public class ComplaintListItemVO {
    // 投诉ID
    private Long id;

    // 投诉标题
    private String title;

    // 投诉描述（可能截断显示）
    private String description;

    // 噪音类型
    private String noiseType;

    // 投诉状态
    private String status;

    // 行政区
    private String district;

    // 投诉创建时间
    private Date createTime;

    // 投诉人真实姓名（管理员视图可见）
    private String userRealName;

    // 处理人员ID
    private Long workerId;

    // 处理人员真实姓名
    private String workerRealName;

    // 更新时间
    private Date updateTime;
}