package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.Rating;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价表 Mapper 接口
 */
@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
    // 基础的评价数据操作已由 BaseMapper 提供
}