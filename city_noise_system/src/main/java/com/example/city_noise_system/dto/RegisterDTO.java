package com.example.city_noise_system.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String realName;
    private String phone;
    // 角色固定为居民，不允许外部修改
    private String role = "RESIDENT";
    private String avatar;

    /**
     * 强制设置角色为居民
     * 确保注册用户只能是居民角色
     */
    public void setRole(String role) {
        // 忽略外部传入的角色值，固定为居民
        this.role = "RESIDENT";
    }
}