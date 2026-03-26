package com.example.city_noise_system.dto;

import lombok.Data;

@Data
public class RatingSubmitDTO {
    private Long complaintId;  // 投诉ID
    private Integer score;     // 评分（1-5分）
    private String comment;    // 评价内容
}