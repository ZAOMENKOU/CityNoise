package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 投诉实体类
 */
@Data
@TableName("complaints")
@EqualsAndHashCode(callSuper = false)
public class Complaint {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("noise_type")
    private String noiseType; // CONSTRUCTION, TRAFFIC, LIVING, COMMERCIAL, INDUSTRIAL

    @TableField("status")
    private String status; // PENDING, ASSIGNED, PROCESSING, RESOLVED, CLOSED

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}