package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("monitor_station")
public class MonitorStation {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("station_name")
    private String stationName;

    @TableField("district")
    private String district;

    @TableField("functional_zone")
    private String functionalZone; // CLASS_1, CLASS_2, CLASS_3, CLASS_4A, CLASS_4B

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("address")
    private String address;

    @TableField("is_active")
    private Boolean isActive = true;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}