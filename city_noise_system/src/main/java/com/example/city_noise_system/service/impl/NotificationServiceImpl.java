package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.dto.NotificationDTO;
import com.example.city_noise_system.entity.Notification;
import com.example.city_noise_system.mapper.NotificationMapper;
import com.example.city_noise_system.service.NotificationService;
import com.example.city_noise_system.vo.NotificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public boolean sendNotification(NotificationDTO notificationDTO) {
        System.out.println("=== 开始发送通知 ===");
        System.out.println("用户ID: " + notificationDTO.getUserId());
        System.out.println("标题: " + notificationDTO.getTitle());
        System.out.println("内容: " + notificationDTO.getContent());
        System.out.println("类型: " + notificationDTO.getType());
        System.out.println("关联ID: " + notificationDTO.getRelatedId());
        
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationDTO, notification);
        boolean saved = save(notification);
        System.out.println("通知发送结果: " + saved);
        System.out.println("=== 通知发送完成 ===");
        return saved;
    }

    @Override
    public boolean sendSystemAnnouncement(String title, String content) {
        System.out.println("=== 开始发送系统公告 ===");
        System.out.println("标题: " + title);
        System.out.println("内容: " + content);
        
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("SYSTEM");
        // null表示发送给所有用户
        notification.setUserId(null);
        boolean saved = save(notification);
        System.out.println("系统公告发送结果: " + saved);
        System.out.println("=== 系统公告发送完成 ===");
        return saved;
    }

    @Override
    public boolean sendTaskNotification(Long userId, String title, String content, Long relatedId) {
        System.out.println("=== 开始发送任务通知 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("标题: " + title);
        System.out.println("内容: " + content);
        System.out.println("关联ID: " + relatedId);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("TASK");
        notification.setRelatedId(relatedId);
        boolean saved = save(notification);
        System.out.println("任务通知发送结果: " + saved);
        System.out.println("=== 任务通知发送完成 ===");
        return saved;
    }

    @Override
    public boolean sendComplaintNotification(Long complaintId, Long userId, String title, String content) {
        System.out.println("=== 开始发送投诉通知 ===");
        System.out.println("投诉ID: " + complaintId);
        System.out.println("用户ID: " + userId);
        System.out.println("标题: " + title);
        System.out.println("内容: " + content);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("COMPLAINT");
        notification.setRelatedId(complaintId);
        boolean saved = save(notification);
        System.out.println("投诉通知发送结果: " + saved);
        System.out.println("=== 投诉通知发送完成 ===");
        return saved;
    }

    @Override
    public Page<NotificationVO> getUserNotifications(Long userId, Integer page, Integer size, Boolean isRead) {
        Page<Notification> notificationPage = new Page<>(page != null ? page : 1, size != null ? size : 10);

        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();

        // 用户特定的通知或者系统通知（userId为null）
        queryWrapper.and(qw -> qw.eq("user_id", userId).or().isNull("user_id"));

        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }

        queryWrapper.orderByDesc("create_time");

        Page<Notification> pageResult = page(notificationPage, queryWrapper);

        Page<NotificationVO> voPage = new Page<>();
        BeanUtils.copyProperties(pageResult, voPage, "records");

        List<NotificationVO> voList = pageResult.getRecords().stream().map(notification -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(notification, vo);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.eq("user_id", userId).or().isNull("user_id"));
        queryWrapper.eq("is_read", false);
        return count(queryWrapper);
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        // 检查用户是否有权限操作此通知
        if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }

        notification.setIsRead(true);
        return updateById(notification);
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.eq("user_id", userId).or().isNull("user_id"));
        queryWrapper.eq("is_read", false);

        List<Notification> notifications = list(queryWrapper);
        notifications.forEach(notification -> notification.setIsRead(true));

        return updateBatchById(notifications);
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        // 1. 验证通知是否存在
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        // 2. 验证用户是否有权限删除此通知
        if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }

        // 3. 执行删除操作
        return removeById(notificationId);
    }
}