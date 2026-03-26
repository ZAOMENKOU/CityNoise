package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("noise_standard")
public class NoiseStandard {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("functional_zone")
    private String functionalZone;

    @TableField("day_limit")
    private BigDecimal dayLimit;

    @TableField("night_limit")
    private BigDecimal nightLimit;

    @TableField("description")
    private String description;

    @TableField("update_time")
    private Date updateTime;
}