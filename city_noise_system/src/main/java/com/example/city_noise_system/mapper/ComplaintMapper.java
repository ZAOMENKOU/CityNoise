package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投诉主表 Mapper 接口
 */
@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
    // 基础的投诉数据操作已由 BaseMapper 提供
}