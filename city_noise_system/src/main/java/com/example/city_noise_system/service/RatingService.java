package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.Rating;

import com.example.city_noise_system.vo.RatingVO;

import java.util.List;

/**
 * 评价服务接口
 */
public interface RatingService extends IService<Rating> {

    /**
     * 添加评价
     */
    boolean addRating(Long complaintId, Long userId, Integer score, String comment);


    /**
     * 获取投诉的评价
     */
    RatingVO getRatingByComplaintId(Long complaintId);

    /**
     * 获取用户的评价列表
     */
    List<RatingVO> getRatingsByUserId(Long userId);

    /**
     * 计算平均评分
     */
    Double getAverageRating(Long workerId);
}