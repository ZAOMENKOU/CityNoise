package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city_noise_system.entity.Complaint;
import com.example.city_noise_system.dto.ComplaintCreateDTO;
import com.example.city_noise_system.vo.ComplaintDetailVO;
import com.example.city_noise_system.vo.ComplaintListItemVO;
import com.example.city_noise_system.vo.ComplaintMapPointVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 投诉服务接口
 */
public interface ComplaintService extends IService<Complaint> {

    /**
     * 创建投诉
     */
    Long createComplaint(ComplaintCreateDTO createDTO, Long userId, List<MultipartFile> images);

    /**
     * 获取投诉详情
     */
    ComplaintDetailVO getComplaintDetail(Long complaintId, Long currentUserId);

    /**
     * 获取我的投诉列表
     */
    Page<ComplaintListItemVO> getMyComplaints(Long userId, Integer page, Integer size, String status);

    /**
     * 获取所有投诉列表（管理员用）
     */
    Page<ComplaintListItemVO> getAllComplaints(Integer page, Integer size, String status, String district,
            String noiseType);

    /**
     * 获取所有投诉列表（管理员用，带处理人员过滤）
     */
    Page<ComplaintListItemVO> getAllComplaints(Integer page, Integer size, String status, String district,
            String noiseType, Long workerId);

    /**
     * 获取地图展示的投诉点
     */
    List<ComplaintMapPointVO> getComplaintMapPoints();

    /**
     * 更新投诉状态
     */
    boolean updateComplaintStatus(Long complaintId, String status, String remark, Long operatorId);

    /**
     * 分配投诉任务
     */
    boolean assignComplaint(Long complaintId, Long workerId, Long adminId);

    /**
     * 处理投诉（处理人员操作）
     */
    boolean processComplaint(Long complaintId, String remark, Long workerId);

    /**
     * 完成投诉处理
     */
    boolean resolveComplaint(Long complaintId, String remark, Long workerId);

    /**
     * 获取处理人员的任务列表
     */
    Page<ComplaintListItemVO> getWorkerTasks(Long workerId, Integer page, Integer size, String status);

    /**
     * 获取投诉的图片列表
     */
    List<String> getComplaintImages(Long complaintId);

    /**
     * 获取附近的投诉点
     */
    List<ComplaintMapPointVO> getNearbyComplaints(double longitude, double latitude, int radius, String noiseType);

    /**
     * 标记投诉为未解决
     */
    boolean markAsUnsolved(Long complaintId, Long userId, String reason);

    /**
     * 按行政区统计投诉数量
     */
    java.util.Map<String, Integer> getComplaintsCountByDistrict();

    /**
     * 按噪音类型统计投诉数量
     */
    java.util.Map<String, Integer> getComplaintsCountByNoiseType();

    /**
     * 删除投诉（居民或管理员操作）
     */
    boolean deleteComplaint(Long complaintId, Long userId, String reason, String role);
}