package com.example.city_noise_system.dto;

import lombok.Data;

/**
 * 问题未解决提交DTO
 * 用于接收居民提交的问题未解决原因
 */
@Data
public class UnsolvedSubmitDTO {
    /**
     * 问题未解决的原因
     */
    private String reason;
}
