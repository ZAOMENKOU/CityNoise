package com.example.city_noise_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ComplaintCreateDTO {
    private String title;
    private String description;
    private String noiseType; // CONSTRUCTION, TRAFFIC, LIVING, COMMERCIAL, INDUSTRIAL
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String district;
    private String detailAddress;
}