// ComplaintDetailVO.java
package com.example.city_noise_system.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ComplaintDetailVO {
    private Long id;
    private String title;
    private String description;
    private String noiseType;
    private String status;
    private Date createTime;
    private Date updateTime;

    // 投诉人信息
    private Long userId;
    private String userRealName;
    private String userPhone;

    // 位置信息
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String district;
    private String detailAddress;

    // 处理记录
    private List<HandlingRecordVO> handlingRecords;

    // 评价信息
    private RatingVO rating;

    // 图片列表
    private List<String> images;
}