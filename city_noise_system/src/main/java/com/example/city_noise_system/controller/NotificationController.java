package com.example.city_noise_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city_noise_system.service.NotificationService;
import com.example.city_noise_system.vo.NotificationVO;
import com.example.city_noise_system.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户的消息列表
     * 
     * @param page    页码
     * @param size    每页大小
     * @param request HttpServletRequest
     * @return 消息列表
     */
    @GetMapping("/list")
    public ResultVO<Page<NotificationVO>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取当前登录用户的ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResultVO.error(401, "用户未认证");
            }

            Page<NotificationVO> notificationPage = notificationService.getUserNotifications(userId, page, size, null);
            return ResultVO.success(notificationPage);
        } catch (Exception e) {
            return ResultVO.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * 
     * @param id      消息ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/{id}/read")
    public ResultVO<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从请求属性中获取当前登录用户的ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResultVO.error(401, "用户未认证");
            }

            boolean success = notificationService.markAsRead(id, userId);
            if (success) {
                return ResultVO.success();
            } else {
                return ResultVO.error(400, "标记已读失败");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     * 
     * @param request HttpServletRequest
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public ResultVO<Long> getUnreadCount(HttpServletRequest request) {
        try {
            // 从请求属性中获取当前登录用户的ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResultVO.error(401, "用户未认证");
            }

            long count = notificationService.getUnreadCount(userId);
            return ResultVO.success(count);
        } catch (Exception e) {
            return ResultVO.error(500, "获取未读消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     * 
     * @param id      通知ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从请求属性中获取当前登录用户的ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResultVO.error(401, "用户未认证");
            }

            boolean success = notificationService.deleteNotification(id, userId);
            if (success) {
                return ResultVO.success();
            } else {
                return ResultVO.error(400, "删除通知失败");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "删除通知失败: " + e.getMessage());
        }
    }
}
