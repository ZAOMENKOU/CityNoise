package com.example.city_noise_system.controller;

import com.example.city_noise_system.entity.Notification;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.mapper.NotificationMapper;
import com.example.city_noise_system.mapper.UserMapper;
import com.example.city_noise_system.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 测试控制器
 * 用于验证系统是否正常运行
 */
@RestController
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 系统健康检查接口
     * GET /
     */
    @GetMapping("/")
    public ResultVO<?> healthCheck() {
        return ResultVO.success("城市噪音投诉管理系统后端服务正常运行！");
    }

    /**
     * 公共测试接口（无需认证）
     * GET /api/test/public
     */
    @GetMapping("/api/test/public")
    public ResultVO<?> publicTest() {
        return ResultVO.success("公共测试接口访问成功");
    }

    /**
     * 需要认证的测试接口
     * GET /api/test/secure
     */
    @GetMapping("/api/test/secure")
    public ResultVO<?> secureTest() {
        return ResultVO.success("需要认证的测试接口访问成功");
    }

    /**
     * 测试接口：查询所有用户和通知
     * GET /api/test/data
     */
    @GetMapping("/api/test/data")
    public ResultVO<?> testData() {
        try {
            // 查询所有用户
            List<User> users = userMapper.selectList(null);
            // 查询所有通知
            List<Notification> notifications = notificationMapper.selectList(null);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("notifications", notifications);

            return ResultVO.success(data);
        } catch (Exception e) {
            return ResultVO.error(500, "获取数据失败: " + e.getMessage());
        }
    }
}