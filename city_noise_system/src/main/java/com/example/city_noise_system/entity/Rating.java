package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 评价实体类
 */
@Data
@TableName("ratings")
@EqualsAndHashCode(callSuper = false)
public class Rating {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("complaint_id")
    private Long complaintId;

    @TableField("score")
    private Integer score;

    @TableField("comment")
    private String comment;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}