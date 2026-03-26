// UserVO.java
package com.example.city_noise_system.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String phone;
    private String avatar;
    private Date createTime;
    private Integer deleted;
}