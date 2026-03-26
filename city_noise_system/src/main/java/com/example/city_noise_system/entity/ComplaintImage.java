package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("complaint_images")
public class ComplaintImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("complaint_id")
    private Long complaintId;

    @TableField("image_url")
    private String imageUrl;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}