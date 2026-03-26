package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 通知实体类
 */
@Data
@TableName("notifications")
@EqualsAndHashCode(callSuper = false)
public class Notification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("type")
    private String type; // SYSTEM, TASK, COMPLAINT

    @TableField("related_id")
    private Long relatedId;

    @TableField("is_read")
    private Boolean isRead;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}