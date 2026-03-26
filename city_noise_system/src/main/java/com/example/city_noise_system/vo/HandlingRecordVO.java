package com.example.city_noise_system.vo;

import lombok.Data;
import java.util.Date;

/**
 * 处理记录视图对象
 * 记录投诉的处理过程，形成时间线
 */
@Data
public class HandlingRecordVO {
    // 处理操作类型：SUBMIT(提交)、ASSIGN(分配)、START_PROCESS(开始处理)、UPDATE(更新)、RESOLVE(解决)、CLOSE(关闭)
    private String action;

    // 处理备注，描述具体做了什么
    private String remark;

    // 处理人姓名
    private String handlerName;

    // 处理时间
    private Date createTime;
}