package com.example.city_noise_system.dto;

import lombok.Data;

/**
 * 任务更新DTO
 * 用于接收前端传递的任务更新参数
 */
@Data
public class TaskUpdateDTO {
    /**
     * 处理备注
     */
    private String remark;
}
