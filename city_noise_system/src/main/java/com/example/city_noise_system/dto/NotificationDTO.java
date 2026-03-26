package com.example.city_noise_system.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long userId; // null表示全体通知
    private String title;
    private String content;
    private String type; // SYSTEM, TASK, COMPLAINT
    private Long relatedId;
}