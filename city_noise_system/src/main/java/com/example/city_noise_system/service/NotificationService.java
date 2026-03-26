package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city_noise_system.entity.Notification;
import com.example.city_noise_system.dto.NotificationDTO;
import com.example.city_noise_system.vo.NotificationVO;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知
     */
    boolean sendNotification(NotificationDTO notificationDTO);

    /**
     * 发送系统公告
     */
    boolean sendSystemAnnouncement(String title, String content);

    /**
     * 发送任务通知
     */
    boolean sendTaskNotification(Long userId, String title, String content, Long relatedId);

    /**
     * 发送投诉状态变更通知
     */
    boolean sendComplaintNotification(Long complaintId, Long userId, String title, String content);

    /**
     * 获取用户的通知列表
     */
    Page<NotificationVO> getUserNotifications(Long userId, Integer page, Integer size, Boolean isRead);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    boolean markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);
}