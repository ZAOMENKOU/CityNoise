package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 处理记录实体类
 */
@Data
@TableName("handling_records")
@EqualsAndHashCode(callSuper = false)
public class HandlingRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("complaint_id")
    private Long complaintId;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("action")
    private String action; // SUBMIT, ASSIGN, START_PROCESS, UPDATE, RESOLVE, CLOSE

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}