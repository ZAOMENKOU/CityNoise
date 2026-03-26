package com.example.city_noise_system.vo;

import lombok.Data;
import java.util.Date;

/**
 * 通知视图对象
 * 系统向用户发送的通知消息
 */
@Data
public class NotificationVO {
    // 通知ID
    private Long id;

    // 通知标题
    private String title;

    // 通知内容
    private String content;

    // 通知类型：SYSTEM(系统)、TASK(任务)、COMPLAINT(投诉)
    private String type;

    // 关联的业务ID（如投诉ID、任务ID）
    private Long relatedId;

    // 是否已读
    private Boolean isRead;

    // 通知创建时间
    private Date createTime;
}