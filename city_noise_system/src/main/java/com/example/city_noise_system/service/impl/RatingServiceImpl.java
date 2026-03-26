package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.city_noise_system.entity.Complaint;
import com.example.city_noise_system.entity.HandlingRecord;
import com.example.city_noise_system.entity.Rating;
import com.example.city_noise_system.mapper.ComplaintMapper;
import com.example.city_noise_system.mapper.RatingMapper;
import com.example.city_noise_system.service.ComplaintService; // 导入投诉服务
import com.example.city_noise_system.service.HandlingRecordService; // 导入处理记录服务
import com.example.city_noise_system.service.RatingService;
import com.example.city_noise_system.vo.RatingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating>
        implements RatingService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    @Lazy // 使用@Lazy注解解决循环依赖问题
    private ComplaintService complaintService; // 注入投诉服务

    @Autowired
    private HandlingRecordService handlingRecordService; // 注入处理记录服务

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRating(Long complaintId, Long userId, Integer score, String comment) {
        // 1. 检查投诉是否存在
        Complaint complaint = complaintService.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 2. 检查用户是否有权限评价（必须是投诉人）
        if (!complaint.getUserId().equals(userId)) {
            throw new RuntimeException("您无权评价此投诉");
        }

        // 3. 检查投诉状态是否为RESOLVED
        if (!"RESOLVED".equals(complaint.getStatus())) {
            throw new RuntimeException("只有已解决的投诉才能评价");
        }

        // 4. 检查是否已评价过
        QueryWrapper<Rating> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complaint_id", complaintId);
        Rating existingRating = getOne(queryWrapper);

        boolean saved;
        if (existingRating != null) {
            // 覆盖评价
            existingRating.setScore(score);
            existingRating.setComment(comment);
            existingRating.setCreateTime(new Date());
            saved = updateById(existingRating);
        } else {
            // 创建新评价
            Rating rating = new Rating();
            rating.setComplaintId(complaintId);
            rating.setScore(score);
            rating.setComment(comment);
            rating.setCreateTime(new Date());
            saved = save(rating);
        }

        // 6. 如果评价成功，更新投诉状态为CLOSED
        if (saved) {
            complaint.setStatus("CLOSED");
            complaint.setUpdateTime(new Date());
            complaintService.updateById(complaint);

            // 查找处理人员ID（从处理记录中查找最近的处理人员操作记录）
            Long workerId = null;
            List<HandlingRecord> records = handlingRecordService.list(
                    new QueryWrapper<HandlingRecord>()
                            .eq("complaint_id", complaintId)
                            .isNotNull("handler_id")
                            .orderByDesc("create_time"));

            if (!records.isEmpty()) {
                // 直接获取第一条记录的handlerId（因为已经按时间倒序排序）
                workerId = records.get(0).getHandlerId();
            }

            // 添加关闭记录
            HandlingRecord closeRecord = new HandlingRecord();
            closeRecord.setComplaintId(complaintId);
            closeRecord.setHandlerId(workerId); // 使用处理人员ID，而不是用户ID
            closeRecord.setAction("CLOSE");
            closeRecord.setRemark("用户评价后关闭投诉");
            closeRecord.setCreateTime(new Date());
            handlingRecordService.save(closeRecord);
        }

        return saved;
    }

    @Override
    public RatingVO getRatingByComplaintId(Long complaintId) {
        QueryWrapper<Rating> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complaint_id", complaintId);
        Rating rating = getOne(queryWrapper);

        if (rating == null) {
            return null;
        }

        RatingVO ratingVO = new RatingVO();
        BeanUtils.copyProperties(rating, ratingVO);
        return ratingVO;
    }

    @Override
    public List<RatingVO> getRatingsByUserId(Long userId) {
        // 1. 先获取该用户的所有投诉
        QueryWrapper<Complaint> complaintQuery = new QueryWrapper<>();
        complaintQuery.eq("user_id", userId);
        List<Complaint> complaints = complaintMapper.selectList(complaintQuery);

        // 2. 提取投诉ID列表
        List<Long> complaintIds = complaints.stream()
                .map(Complaint::getId)
                .collect(Collectors.toList());

        if (complaintIds.isEmpty()) {
            return List.of();
        }

        // 3. 获取这些投诉的评价
        QueryWrapper<Rating> ratingQuery = new QueryWrapper<>();
        ratingQuery.in("complaint_id", complaintIds);
        List<Rating> ratings = list(ratingQuery);

        // 4. 转换为VO
        return ratings.stream().map(rating -> {
            RatingVO ratingVO = new RatingVO();
            BeanUtils.copyProperties(rating, ratingVO);
            return ratingVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Double getAverageRating(Long workerId) {
        // 1. 获取该处理人员处理过的所有投诉
        // 注意：这里需要关联handling_records表，查找该处理人员处理过的投诉

        // 由于目前数据库设计中没有直接关联，这里简化处理
        // 实际应该通过handling_records表关联查询

        // 2. 获取这些投诉的评价
        // 3. 计算平均分

        // 简化实现：返回null，表示暂时无法计算
        return null;
    }
}