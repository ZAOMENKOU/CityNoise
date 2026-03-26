package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 投诉位置实体类
 */
@Data
@TableName("complaint_locations")
@EqualsAndHashCode(callSuper = false)
public class ComplaintLocation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("complaint_id")
    private Long complaintId;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("district")
    private String district;

    @TableField("detail_address")
    private String detailAddress;
}