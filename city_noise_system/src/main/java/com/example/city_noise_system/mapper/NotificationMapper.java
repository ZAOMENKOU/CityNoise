package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知表 Mapper 接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    // 基础的通知数据操作已由 BaseMapper 提供
}