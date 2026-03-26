package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.HandlingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处理记录表 Mapper 接口
 */
@Mapper
public interface HandlingRecordMapper extends BaseMapper<HandlingRecord> {
    // 基础的处理日志操作已由 BaseMapper 提供
}