// RatingVO.java
package com.example.city_noise_system.vo;

import lombok.Data;
import java.util.Date;

@Data
public class RatingVO {
    private Integer score;
    private String comment;
    private Date createTime;
}