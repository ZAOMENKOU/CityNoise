package com.example.city_noise_system.controller;

import com.example.city_noise_system.dto.LoginDTO;
import com.example.city_noise_system.dto.RegisterDTO;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.service.UserService;
import com.example.city_noise_system.utils.JwtUtil;
import com.example.city_noise_system.vo.ResultVO;
import com.example.city_noise_system.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

/**
 * 认证控制器
 * 处理用户登录、注册、获取用户信息等认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResultVO<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 1. 参数校验
            if (!StringUtils.hasText(loginDTO.getUsername())) {
                return ResultVO.error(400, "用户名不能为空");
            }

            if (!StringUtils.hasText(loginDTO.getPassword())) {
                return ResultVO.error(400, "密码不能为空");
            }

            // 2. 调用Service进行登录验证
            UserVO userVO = userService.login(loginDTO);

            // 3. 生成JWT Token
            String token = jwtUtil.generateToken(
                    userVO.getId(),
                    userVO.getUsername(),
                    userVO.getRole());

            // 4. 构建返回数据
            UserVO responseUser = new UserVO();
            BeanUtils.copyProperties(userVO, responseUser);

            // 创建返回结果对象
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserInfo(responseUser);

            return ResultVO.success(response);

        } catch (RuntimeException e) {
            // 登录失败
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            // 系统异常
            return ResultVO.error(500, "登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册接口
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResultVO<?> register(@RequestBody RegisterDTO registerDTO) {
        try {
            // 1. 参数校验
            if (!StringUtils.hasText(registerDTO.getUsername())) {
                return ResultVO.error(400, "用户名不能为空");
            }

            if (!StringUtils.hasText(registerDTO.getPassword())) {
                return ResultVO.error(400, "密码不能为空");
            }

            if (!StringUtils.hasText(registerDTO.getRealName())) {
                return ResultVO.error(400, "真实姓名不能为空");
            }

            // 2. 调用Service进行注册
            UserVO userVO = userService.register(registerDTO);

            // 3. 生成JWT Token
            String token = jwtUtil.generateToken(
                    userVO.getId(),
                    userVO.getUsername(),
                    userVO.getRole());

            // 4. 构建返回数据
            RegisterResponse response = new RegisterResponse();
            response.setToken(token);
            response.setUserInfo(userVO);

            return ResultVO.success(response, "注册成功");

        } catch (RuntimeException e) {
            // 注册失败（如用户名已存在）
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            // 系统异常
            return ResultVO.error(500, "注册失败，请稍后重试");
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/me
     * 需要JWT Token认证
     */
    @GetMapping("/me")
    public ResultVO<?> getCurrentUser(@RequestAttribute Long userId) {
        try {
            // 从请求属性中获取用户ID（由JwtAuthenticationFilter设置）
            UserVO userVO = userService.getCurrentUserInfo(userId);
            return ResultVO.success(userVO);
        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "获取用户信息失败");
        }
    }

    /**
     * 更新用户信息
     * PUT /api/auth/me
     * 需要JWT Token认证
     */
    @PutMapping("/me")
    public ResultVO<?> updateUserInfo(
            @RequestAttribute Long userId,
            @RequestBody User user) {
        try {
            // 只允许更新部分字段
            User updateUser = new User();
            updateUser.setRealName(user.getRealName());
            updateUser.setPhone(user.getPhone());
            updateUser.setAvatar(user.getAvatar());

            boolean updated = userService.updateUserInfo(userId, updateUser);
            if (updated) {
                return ResultVO.success("用户信息更新成功");
            } else {
                return ResultVO.error(500, "用户信息更新失败");
            }
        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "更新用户信息失败");
        }
    }

    /**
     * 登出接口
     * POST /api/auth/logout
     * 需要JWT Token认证
     * 注意：JWT是无状态的，登出通常由前端删除Token实现
     */
    @PostMapping("/logout")
    public ResultVO<?> logout() {
        // JWT是无状态的，登出操作在前端进行（删除Token）
        return ResultVO.success("登出成功");
    }

    // ==================== 内部响应类 ====================

    /**
     * 登录响应类
     */
    public static class LoginResponse {
        private String token;
        private UserVO userInfo;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserVO getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserVO userInfo) {
            this.userInfo = userInfo;
        }
    }

    /**
     * 注册响应类
     */
    public static class RegisterResponse {
        private String token;
        private UserVO userInfo;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserVO getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserVO userInfo) {
            this.userInfo = userInfo;
        }
    }
}