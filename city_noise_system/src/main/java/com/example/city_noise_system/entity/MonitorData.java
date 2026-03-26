package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("monitor_data")
public class MonitorData {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("station_id")
    private Long stationId;

    @TableField("noise_level")
    private BigDecimal noiseLevel;

    @TableField("monitor_time")
    private Date monitorTime;

    @TableField("monitor_date")
    private Date monitorDate;

    @TableField("period_type")
    private String periodType; // DAY, NIGHT

    @TableField("data_source")
    private String dataSource = "SIMULATED";

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}