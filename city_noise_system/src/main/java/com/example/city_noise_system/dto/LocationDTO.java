package com.example.city_noise_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationDTO {
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String district;
    private String detailAddress;
}