package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.dto.LoginDTO;
import com.example.city_noise_system.dto.RegisterDTO;
import com.example.city_noise_system.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    UserVO login(LoginDTO loginDTO);

    /**
     * 根据用户名获取用户信息
     */
    User getByUsername(String username);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(Long userId, User user);
}