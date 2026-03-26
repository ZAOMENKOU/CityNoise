package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.ComplaintLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投诉位置表 Mapper 接口
 */
@Mapper
public interface ComplaintLocationMapper extends BaseMapper<ComplaintLocation> {
    // 基础的坐标、地址数据操作已由 BaseMapper 提供
}