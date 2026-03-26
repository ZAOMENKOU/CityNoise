package com.example.city_noise_system.controller;

import com.example.city_noise_system.service.MonitorDataService;
import com.example.city_noise_system.service.MonitorStationService;
import com.example.city_noise_system.service.NotificationService;
import com.example.city_noise_system.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 公共控制器
 * 处理一些公共的、不特定于某个角色的接口
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitorStationService monitorStationService;

    @Autowired
    private MonitorDataService monitorDataService;

    /**
     * 获取我的通知列表
     * GET /api/common/notifications
     * 需要JWT Token认证
     */
    @GetMapping("/notifications")
    public ResultVO<?> getMyNotifications(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean isRead) {
        try {
            var notifications = notificationService.getUserNotifications(userId, page, size, isRead);
            return ResultVO.success(notifications);
        } catch (Exception e) {
            return ResultVO.error(500, "获取通知列表失败");
        }
    }

    /**
     * 获取未读通知数量
     * GET /api/common/notifications/unread-count
     * 需要JWT Token认证
     */
    @GetMapping("/notifications/unread-count")
    public ResultVO<?> getUnreadCount(@RequestAttribute Long userId) {
        try {
            Long count = notificationService.getUnreadCount(userId);
            return ResultVO.success(count);
        } catch (Exception e) {
            return ResultVO.error(500, "获取未读通知数量失败");
        }
    }

    /**
     * 标记通知为已读
     * PUT /api/common/notifications/{id}/read
     * 需要JWT Token认证
     */
    @PutMapping("/notifications/{id}/read")
    public ResultVO<?> markAsRead(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        try {
            boolean success = notificationService.markAsRead(id, userId);
            if (success) {
                return ResultVO.success("通知已标记为已读");
            } else {
                return ResultVO.error(500, "标记通知失败");
            }
        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "标记通知失败");
        }
    }

    /**
     * 标记所有通知为已读
     * PUT /api/common/notifications/read-all
     * 需要JWT Token认证
     */
    @PutMapping("/notifications/read-all")
    public ResultVO<?> markAllAsRead(@RequestAttribute Long userId) {
        try {
            boolean success = notificationService.markAllAsRead(userId);
            if (success) {
                return ResultVO.success("所有通知已标记为已读");
            } else {
                return ResultVO.error(500, "标记所有通知失败");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "标记所有通知失败");
        }
    }

    /**
     * 获取监测点列表
     * GET /api/common/monitor-stations
     * 需要JWT Token认证
     */
    @GetMapping("/monitor-stations")
    public ResultVO<?> getMonitorStations(
            @RequestParam(required = false) String district) {
        try {
            var stations = monitorStationService.getByDistrict(district);
            return ResultVO.success(stations);
        } catch (Exception e) {
            return ResultVO.error(500, "获取监测点列表失败");
        }
    }

    /**
     * 生成模拟监测数据
     * POST /api/common/monitor-data/simulate
     * 需要JWT Token认证（管理员角色）
     */
    @PostMapping("/monitor-data/simulate")
    public ResultVO<?> generateSimulatedData(@RequestAttribute String role) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 生成模拟数据
            monitorDataService.generateSimulatedData();

            return ResultVO.success("模拟数据生成成功");

        } catch (Exception e) {
            return ResultVO.error(500, "生成模拟数据失败");
        }
    }

    /**
     * 获取首页概览数据
     * GET /api/common/dashboard/overview
     * 需要JWT Token认证
     */
    @GetMapping("/dashboard/overview")
    public ResultVO<?> getDashboardOverview(
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        try {
            DashboardOverviewResponse response = new DashboardOverviewResponse();

            // 根据角色返回不同的概览数据
            if ("RESIDENT".equals(role)) {
                response.setUserRole("resident");
                response.setMessage("居民概览数据待实现");
            } else if ("WORKER".equals(role)) {
                response.setUserRole("worker");
                response.setMessage("处理人员概览数据待实现");
            } else if ("ADMIN".equals(role)) {
                response.setUserRole("admin");
                response.setMessage("管理员概览数据待实现");
            }

            return ResultVO.success(response);

        } catch (Exception e) {
            return ResultVO.error(500, "获取概览数据失败");
        }
    }

    /**
     * 获取所有行政区列表
     * GET /api/common/districts
     * 需要JWT Token认证
     */
    @GetMapping("/districts")
    public ResultVO<?> getDistricts() {
        try {
            // 武汉市区列表
            List<DistrictResponse> districts = List.of(
                new DistrictResponse("江岸区", "001"),
                new DistrictResponse("江汉区", "002"),
                new DistrictResponse("硚口区", "003"),
                new DistrictResponse("汉阳区", "004"),
                new DistrictResponse("武昌区", "005"),
                new DistrictResponse("青山区", "006"),
                new DistrictResponse("洪山区", "007"),
                new DistrictResponse("东西湖区", "008"),
                new DistrictResponse("汉南区", "009"),
                new DistrictResponse("蔡甸区", "010"),
                new DistrictResponse("江夏区", "011"),
                new DistrictResponse("黄陂区", "012"),
                new DistrictResponse("新洲区", "013"),
                new DistrictResponse("东湖新技术开发区", "014")
            );

            return ResultVO.success(districts);

        } catch (Exception e) {
            return ResultVO.error(500, "获取行政区列表失败");
        }
    }

    // ==================== 内部响应类 ====================

    /**
     * 首页概览响应类
     */
    public static class DashboardOverviewResponse {
        private String userRole;
        private String message;

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 行政区响应类
     */
    public static class DistrictResponse {
        private String name;
        private String code;

        public DistrictResponse(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}