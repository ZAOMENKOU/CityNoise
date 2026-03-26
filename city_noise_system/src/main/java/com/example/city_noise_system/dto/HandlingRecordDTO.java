package com.example.city_noise_system.dto;

import lombok.Data;

@Data
public class HandlingRecordDTO {
    private Long complaintId;
    private Long handlerId;
    private String action;
    private String remark;
}