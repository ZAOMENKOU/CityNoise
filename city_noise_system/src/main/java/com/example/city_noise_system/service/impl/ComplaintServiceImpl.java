package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.dto.ComplaintCreateDTO;
import com.example.city_noise_system.entity.*;
import com.example.city_noise_system.mapper.*;
import com.example.city_noise_system.service.*;
import com.example.city_noise_system.utils.FileUploadUtil;
import com.example.city_noise_system.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

    @Autowired
    private ComplaintLocationService complaintLocationService;

    @Autowired
    private HandlingRecordService handlingRecordService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private ComplaintImageService complaintImageService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComplaint(ComplaintCreateDTO createDTO, Long userId, List<MultipartFile> images) {
        // 1. 创建投诉记录
        Complaint complaint = new Complaint();
        BeanUtils.copyProperties(createDTO, complaint);
        complaint.setUserId(userId);
        complaint.setStatus("PENDING");

        if (!StringUtils.hasText(complaint.getNoiseType())) {
            complaint.setNoiseType("LIVING");
        }

        boolean saved = save(complaint);
        if (!saved) {
            throw new RuntimeException("创建投诉失败");
        }

        // 2. 保存位置信息
        ComplaintLocation location = new ComplaintLocation();
        location.setComplaintId(complaint.getId());
        location.setLongitude(createDTO.getLongitude());
        location.setLatitude(createDTO.getLatitude());
        location.setDistrict(createDTO.getDistrict());
        location.setDetailAddress(createDTO.getDetailAddress());

        boolean locationSaved = complaintLocationService.save(location);
        if (!locationSaved) {
            throw new RuntimeException("保存位置信息失败");
        }

        // 3. 保存图片
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    String imageUrl = fileUploadUtil.uploadImage(image);
                    complaintImageService.saveImage(complaint.getId(), imageUrl);
                } catch (Exception e) {
                    // 图片上传失败不影响投诉创建
                    e.printStackTrace();
                }
            }
        }

        // 4. 添加处理记录
        handlingRecordService.addRecord(complaint.getId(), userId, "SUBMIT", "提交投诉");

        return complaint.getId();
    }

    @Override
    public ComplaintDetailVO getComplaintDetail(Long complaintId, Long currentUserId) {
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 权限检查
        boolean canView = complaint.getUserId().equals(currentUserId);
        if (!canView) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null
                    && ("ADMIN".equals(currentUser.getRole()) || "WORKER".equals(currentUser.getRole()))) {
                canView = true;
            }
        }

        if (!canView) {
            throw new RuntimeException("无权查看此投诉");
        }

        // 构建详情VO
        ComplaintDetailVO detailVO = new ComplaintDetailVO();
        BeanUtils.copyProperties(complaint, detailVO);
        // 设置投诉人ID
        detailVO.setUserId(complaint.getUserId());

        // 获取投诉人信息
        User user = userMapper.selectById(complaint.getUserId());
        if (user != null) {
            detailVO.setUserRealName(user.getRealName());
            detailVO.setUserPhone(user.getPhone());
        }

        // 获取位置信息
        ComplaintLocation location = complaintLocationService.getByComplaintId(complaintId);
        if (location != null) {
            detailVO.setLongitude(location.getLongitude());
            detailVO.setLatitude(location.getLatitude());
            detailVO.setDistrict(location.getDistrict());
            detailVO.setDetailAddress(location.getDetailAddress());
        }

        // 获取处理记录
        List<HandlingRecordVO> handlingRecords = handlingRecordService.getRecordsByComplaintId(complaintId);
        detailVO.setHandlingRecords(handlingRecords);

        // 获取评价信息
        RatingVO rating = ratingService.getRatingByComplaintId(complaintId);
        detailVO.setRating(rating);

        // 获取图片列表
        List<String> images = complaintImageService.getImagesByComplaintId(complaintId);
        detailVO.setImages(images);

        return detailVO;
    }

    @Override
    public Page<ComplaintListItemVO> getMyComplaints(Long userId, Integer page, Integer size, String status) {
        Page<Complaint> complaintPage = new Page<>(page != null ? page : 1, size != null ? size : 10);

        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("create_time");

        Page<Complaint> pageResult = page(complaintPage, queryWrapper);

        Page<ComplaintListItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(pageResult, voPage, "records");

        List<ComplaintListItemVO> voList = pageResult.getRecords().stream().map(complaint -> {
            ComplaintListItemVO vo = new ComplaintListItemVO();
            BeanUtils.copyProperties(complaint, vo);

            ComplaintLocation location = complaintLocationService.getByComplaintId(complaint.getId());
            if (location != null) {
                vo.setDistrict(location.getDistrict());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<ComplaintListItemVO> getAllComplaints(Integer page, Integer size, String status, String district,
            String noiseType) {
        return getAllComplaints(page, size, status, district, noiseType, null);
    }

    @Override
    public Page<ComplaintListItemVO> getAllComplaints(Integer page, Integer size, String status, String district,
            String noiseType, Long workerId) {
        Page<Complaint> complaintPage = new Page<>(page != null ? page : 1, size != null ? size : 10);

        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        if (StringUtils.hasText(noiseType)) {
            queryWrapper.eq("noise_type", noiseType);
        }

        queryWrapper.orderByDesc("create_time");

        Page<Complaint> pageResult = page(complaintPage, queryWrapper);

        Page<ComplaintListItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(pageResult, voPage, "records");

        List<ComplaintListItemVO> voList = pageResult.getRecords().stream().map(complaint -> {
            ComplaintListItemVO vo = new ComplaintListItemVO();
            BeanUtils.copyProperties(complaint, vo);

            User user = userMapper.selectById(complaint.getUserId());
            if (user != null) {
                vo.setUserRealName(user.getRealName());
            }

            ComplaintLocation location = complaintLocationService.getByComplaintId(complaint.getId());
            if (location != null) {
                vo.setDistrict(location.getDistrict());
            }

            // 获取处理人员信息
            List<HandlingRecord> records = handlingRecordService.list(
                    new QueryWrapper<HandlingRecord>()
                            .eq("complaint_id", complaint.getId())
                            .isNotNull("handler_id")
                            .orderByDesc("create_time")
                            .last("LIMIT 1"));

            if (!records.isEmpty()) {
                Long handlerId = records.get(0).getHandlerId();
                User worker = userMapper.selectById(handlerId);
                if (worker != null) {
                    vo.setWorkerId(handlerId);
                    vo.setWorkerRealName(worker.getRealName());
                }
            }

            // 设置更新时间
            vo.setUpdateTime(complaint.getUpdateTime());

            return vo;
        }).collect(Collectors.toList());

        // 如果指定了处理人员ID，过滤结果
        if (workerId != null) {
            voList = voList.stream()
                    .filter(vo -> workerId.equals(vo.getWorkerId()))
                    .collect(Collectors.toList());
            voPage.setTotal(voList.size());
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ComplaintMapPointVO> getComplaintMapPoints() {
        System.out.println("=== 开始获取投诉地图点 ===");
        // 获取所有未删除的投诉，与投诉管理页面保持一致
        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        List<Complaint> complaints = list(queryWrapper);
        System.out.println("获取到的投诉数量: " + complaints.size());

        List<ComplaintMapPointVO> mapPoints = new ArrayList<>();

        for (Complaint complaint : complaints) {
            System.out.println("处理投诉: id=" + complaint.getId() + ", status=" + complaint.getStatus() + ", deleted="
                    + complaint.getDeleted());
            // 获取投诉位置
            ComplaintLocation location = complaintLocationService.getByComplaintId(complaint.getId());
            if (location != null) {
                System.out.println("找到投诉位置: complaintId=" + complaint.getId());
                ComplaintMapPointVO pointVO = new ComplaintMapPointVO();
                pointVO.setId(complaint.getId());
                pointVO.setTitle(complaint.getTitle());
                pointVO.setStatus(complaint.getStatus());
                pointVO.setLongitude(location.getLongitude());
                pointVO.setLatitude(location.getLatitude());
                pointVO.setDistrict(location.getDistrict());
                pointVO.setNoiseType(complaint.getNoiseType());
                if (complaint.getCreateTime() != null) {
                    pointVO.setCreateTime(complaint.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime());
                }

                mapPoints.add(pointVO);
                System.out.println("添加投诉点: id=" + complaint.getId() + ", title=" + complaint.getTitle());
            } else {
                System.out.println("未找到投诉位置: complaintId=" + complaint.getId());
            }
        }

        System.out.println("最终返回的投诉点数量: " + mapPoints.size());
        System.out.println("=== 获取投诉地图点完成 ===");
        return mapPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateComplaintStatus(Long complaintId, String status, String remark, Long operatorId) {
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 检查是否是首次进入处理状态
        boolean isInitialProcessing = !"PROCESSING".equals(complaint.getStatus()) && "PROCESSING".equals(status);

        complaint.setStatus(status);

        boolean updated = updateById(complaint);
        if (!updated) {
            throw new RuntimeException("更新状态失败");
        }

        String action = "UPDATE";
        if ("ASSIGNED".equals(status)) {
            action = "ASSIGN";
            // 发送通知给处理人员和居民
            sendAssignmentNotifications(complaintId, complaint.getUserId(), operatorId, remark);
        } else if ("PROCESSING".equals(status)) {
            if (isInitialProcessing) {
                action = "START_PROCESS";
                // 发送通知给居民
                sendStatusChangeNotification(complaintId, complaint.getUserId(), "投诉处理开始", "您的投诉已开始处理，请耐心等待");
            } else {
                action = "UPDATE";
                // 发送通知给居民
                sendStatusChangeNotification(complaintId, complaint.getUserId(), "处理进度更新", "您的投诉处理进度已更新: " + remark);
            }
        } else if ("RESOLVED".equals(status)) {
            action = "RESOLVE";
            // 发送通知给居民
            sendStatusChangeNotification(complaintId, complaint.getUserId(), "投诉处理完成", "您的投诉已处理完成，感谢您的反馈");
        } else if ("CLOSED".equals(status)) {
            action = "CLOSE";
            // 发送通知给居民
            sendStatusChangeNotification(complaintId, complaint.getUserId(), "投诉已关闭", "您的投诉已关闭，如有疑问请联系管理员");
        }

        handlingRecordService.addRecord(complaintId, operatorId, action, remark);

        return true;
    }

    /**
     * 发送任务分配通知
     */
    private void sendAssignmentNotifications(Long complaintId, Long residentId, Long operatorId, String remark) {
        System.out.println("=== 开始发送任务分配通知 ===");
        System.out.println("投诉ID: " + complaintId);
        System.out.println("居民ID: " + residentId);
        System.out.println("操作员ID: " + operatorId);
        System.out.println("备注: " + remark);

        // 1. 发送通知给处理人员（从remark中提取处理人员信息）
        if (remark != null && remark.contains("处理人员：")) {
            int startIndex = remark.indexOf("处理人员：") + 5;
            String workerName = remark.substring(startIndex);
            System.out.println("处理人员姓名: " + workerName);

            // 查找处理人员ID
            User worker = userMapper.selectOne(new QueryWrapper<User>().eq("real_name", workerName));
            if (worker != null) {
                System.out.println("找到处理人员: " + worker.getRealName() + " (ID: " + worker.getId() + ")");
                boolean sent = notificationService.sendTaskNotification(
                        worker.getId(),
                        "新任务分配",
                        "您有新的噪音投诉处理任务，请及时处理",
                        complaintId);
                System.out.println("处理人员通知发送结果: " + sent);
            } else {
                System.out.println("未找到处理人员: " + workerName);
            }
        }

        // 2. 发送通知给居民
        boolean residentSent = sendStatusChangeNotification(complaintId, residentId, "投诉已分配", "您的投诉已分配给处理人员，我们会尽快处理");
        System.out.println("居民通知发送结果: " + residentSent);
        System.out.println("=== 任务分配通知发送完成 ===");
    }

    /**
     * 发送状态变更通知
     */
    private boolean sendStatusChangeNotification(Long complaintId, Long userId, String title, String content) {
        System.out.println("=== 开始发送状态变更通知 ===");
        System.out.println("投诉ID: " + complaintId);
        System.out.println("用户ID: " + userId);
        System.out.println("标题: " + title);
        System.out.println("内容: " + content);

        boolean sent = notificationService.sendComplaintNotification(complaintId, userId, title, content);
        System.out.println("状态变更通知发送结果: " + sent);
        System.out.println("=== 状态变更通知发送完成 ===");
        return sent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignComplaint(Long complaintId, Long workerId, Long adminId) {
        User worker = userMapper.selectById(workerId);
        if (worker == null || !"WORKER".equals(worker.getRole())) {
            throw new RuntimeException("指定的处理人员不存在或不是处理人员角色");
        }

        // 获取投诉信息
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 构建分配备注
        String remark = String.format("已分配给处理人员：%s", worker.getRealName());

        boolean assigned = updateComplaintStatus(complaintId, "ASSIGNED", remark, workerId);

        if (assigned) {
            // 通知已经在updateComplaintStatus中发送
        }

        return assigned;
    }

    @Override
    public boolean processComplaint(Long complaintId, String remark, Long workerId) {
        return updateComplaintStatus(complaintId, "PROCESSING", remark, workerId);
    }

    @Override
    public boolean resolveComplaint(Long complaintId, String remark, Long workerId) {
        return updateComplaintStatus(complaintId, "RESOLVED", remark, workerId);
    }

    @Override
    public List<String> getComplaintImages(Long complaintId) {
        return complaintImageService.getImagesByComplaintId(complaintId);
    }

    @Override
    public Page<ComplaintListItemVO> getWorkerTasks(Long workerId, Integer page, Integer size, String status) {
        Page<Complaint> complaintPage = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 构建子查询：查找分配给该处理人员的投诉ID
        QueryWrapper<HandlingRecord> recordQueryWrapper = new QueryWrapper<>();
        recordQueryWrapper.eq("handler_id", workerId);
        recordQueryWrapper.eq("action", "ASSIGN");
        List<HandlingRecord> records = handlingRecordService.list(recordQueryWrapper);

        List<Long> complaintIds = records.stream()
                .map(HandlingRecord::getComplaintId)
                .distinct()
                .collect(Collectors.toList());

        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        if (!complaintIds.isEmpty()) {
            queryWrapper.in("id", complaintIds);
        } else {
            // 如果没有分配的任务，返回空列表
            queryWrapper.eq("id", -1);
        }

        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("create_time");

        Page<Complaint> pageResult = page(complaintPage, queryWrapper);

        Page<ComplaintListItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(pageResult, voPage, "records");

        List<ComplaintListItemVO> voList = pageResult.getRecords().stream().map(complaint -> {
            ComplaintListItemVO vo = new ComplaintListItemVO();
            BeanUtils.copyProperties(complaint, vo);

            ComplaintLocation location = complaintLocationService.getByComplaintId(complaint.getId());
            if (location != null) {
                vo.setDistrict(location.getDistrict());
            }

            User user = userMapper.selectById(complaint.getUserId());
            if (user != null) {
                vo.setUserRealName(user.getRealName());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ComplaintMapPointVO> getNearbyComplaints(double longitude, double latitude, int radius,
            String noiseType) {
        List<ComplaintLocation> locations = complaintLocationService.getAllForMap();
        List<ComplaintMapPointVO> nearbyPoints = new ArrayList<>();

        for (ComplaintLocation location : locations) {
            Complaint complaint = getById(location.getComplaintId());
            if (complaint != null && !"CLOSED".equals(complaint.getStatus())) {
                // 按噪音类型过滤
                if (StringUtils.hasText(noiseType) && !noiseType.equals(complaint.getNoiseType())) {
                    continue;
                }

                // 计算距离
                double distance = calculateDistance(
                        latitude,
                        longitude,
                        location.getLatitude().doubleValue(),
                        location.getLongitude().doubleValue());

                // 过滤在指定半径内的投诉点
                if (distance <= radius) {
                    ComplaintMapPointVO pointVO = new ComplaintMapPointVO();
                    pointVO.setId(complaint.getId());
                    pointVO.setTitle(complaint.getTitle());
                    pointVO.setStatus(complaint.getStatus());
                    pointVO.setLongitude(location.getLongitude());
                    pointVO.setLatitude(location.getLatitude());
                    pointVO.setDistrict(location.getDistrict());
                    pointVO.setDistance((int) Math.round(distance)); // 距离，单位米
                    if (complaint.getCreateTime() != null) {
                        pointVO.setCreateTime(complaint.getCreateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    }
                    pointVO.setNoiseType(complaint.getNoiseType());

                    nearbyPoints.add(pointVO);
                }
            }
        }

        return nearbyPoints;
    }

    /**
     * 使用Haversine公式计算两个经纬度之间的距离（单位：米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsUnsolved(Long complaintId, Long userId, String reason) {
        // 1. 验证投诉是否存在
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 2. 验证用户是否有权限（必须是投诉人）
        if (!complaint.getUserId().equals(userId)) {
            throw new RuntimeException("您无权操作此投诉");
        }

        // 3. 验证投诉状态是否为已解决
        if (!"RESOLVED".equals(complaint.getStatus())) {
            throw new RuntimeException("只有已解决的投诉才能标记为未解决");
        }

        // 4. 更新投诉状态为处理中
        complaint.setStatus("PROCESSING");
        boolean updated = updateById(complaint);
        if (!updated) {
            throw new RuntimeException("更新状态失败");
        }

        // 5. 查找处理人员ID（从处理记录中查找最近的处理人员操作记录）
        System.out.println("=== 开始查找处理人员ID ===");
        System.out.println("投诉ID: " + complaintId);

        // 查找所有有处理人员的记录，按创建时间倒序
        List<HandlingRecord> records = handlingRecordService.list(
                new QueryWrapper<HandlingRecord>()
                        .eq("complaint_id", complaintId)
                        .isNotNull("handler_id")
                        .orderByDesc("create_time"));

        Long workerId = null;
        System.out.println("找到的处理记录数量: " + records.size());
        if (!records.isEmpty()) {
            // 直接获取第一条记录的handlerId（因为已经按时间倒序排序）
            HandlingRecord latestRecord = records.get(0);
            workerId = latestRecord.getHandlerId();
            System.out.println("找到的处理人员ID: " + workerId);
            System.out.println("处理记录action: " + latestRecord.getAction());
            System.out.println("处理记录remark: " + latestRecord.getRemark());
        } else {
            System.out.println("=== 未找到处理记录 ===");
            System.out.println("投诉ID: " + complaintId);
        }

        // 6. 添加处理记录
        String remark = "居民标记为未解决：" + reason;
        handlingRecordService.addRecord(complaintId, workerId, "UPDATE", remark);

        // 7. 发送通知给处理人员
        if (workerId != null) {
            boolean sent = notificationService.sendTaskNotification(
                    workerId,
                    "投诉未解决",
                    "您处理的投诉被标记为未解决，请重新处理",
                    complaintId);
            System.out.println("=== 发送通知给处理人员 ===");
            System.out.println("处理人员ID: " + workerId);
            System.out.println("通知发送结果: " + sent);
            System.out.println("通知内容: 您处理的投诉被标记为未解决，请重新处理");
        } else {
            System.out.println("=== 未找到处理人员，无法发送通知 ===");
            System.out.println("投诉ID: " + complaintId);
        }

        // 8. 发送通知给居民
        boolean residentSent = notificationService.sendComplaintNotification(
                complaintId,
                userId,
                "投诉状态更新",
                "您的投诉已标记为未解决，处理人员将重新处理");
        System.out.println("=== 发送通知给居民 ===");
        System.out.println("居民ID: " + userId);
        System.out.println("通知发送结果: " + residentSent);
        System.out.println("通知内容: 您的投诉已标记为未解决，处理人员将重新处理");

        return true;
    }

    @Override
    public Map<String, Integer> getComplaintsCountByDistrict() {
        // 使用MyBatis Plus的分组查询，包含所有状态的投诉
        // 先获取所有未删除的投诉ID
        QueryWrapper<Complaint> complaintQueryWrapper = new QueryWrapper<>();
        complaintQueryWrapper.select("id");
        complaintQueryWrapper.eq("deleted", 0);
        List<Complaint> allComplaints = list(complaintQueryWrapper);

        List<Long> allComplaintIds = allComplaints.stream()
                .map(Complaint::getId)
                .collect(Collectors.toList());

        // 使用MyBatis Plus的分组查询
        QueryWrapper<ComplaintLocation> queryWrapper = new QueryWrapper<>();
        if (!allComplaintIds.isEmpty()) {
            queryWrapper.in("complaint_id", allComplaintIds);
        } else {
            // 如果没有投诉，返回空结果
            return new HashMap<>();
        }
        queryWrapper.select("district, count(*) as count");
        queryWrapper.groupBy("district");

        List<Map<String, Object>> result = complaintLocationService.getBaseMapper().selectMaps(queryWrapper);

        // 转换为Map<String, Integer>
        Map<String, Integer> districtStats = new HashMap<>();
        for (Map<String, Object> item : result) {
            String district = (String) item.get("district");
            Integer count = ((Number) item.get("count")).intValue();
            districtStats.put(district, count);
        }

        return districtStats;
    }

    @Override
    public Map<String, Integer> getComplaintsCountByNoiseType() {
        // 使用MyBatis Plus的分组查询，包含所有状态的投诉
        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("noise_type, count(*) as count");
        queryWrapper.eq("deleted", 0);
        queryWrapper.groupBy("noise_type");

        List<Map<String, Object>> result = baseMapper.selectMaps(queryWrapper);

        // 转换为Map<String, Integer>
        Map<String, Integer> noiseTypeStats = new HashMap<>();
        for (Map<String, Object> item : result) {
            String noiseType = (String) item.get("noise_type");
            Integer count = ((Number) item.get("count")).intValue();
            noiseTypeStats.put(noiseType, count);
        }

        return noiseTypeStats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComplaint(Long complaintId, Long userId, String reason, String role) {
        // 1. 验证投诉是否存在
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }

        // 2. 验证用户是否有权限
        // 管理员可以删除任何投诉，居民只能删除自己的投诉
        if (!"ADMIN".equals(role) && !complaint.getUserId().equals(userId)) {
            throw new RuntimeException("您无权操作此投诉");
        }

        // 3. 验证投诉状态是否为待处理
        if (!"PENDING".equals(complaint.getStatus())) {
            throw new RuntimeException("只有待处理的投诉才能删除");
        }

        // 4. 添加处理记录
        String remark = "ADMIN".equals(role) ? "管理员删除投诉：" + reason : "居民删除投诉：" + reason;
        handlingRecordService.addRecord(complaintId, userId, "CLOSE", remark);

        // 5. 发送通知给居民（投诉的创建者）
        boolean sent = notificationService.sendComplaintNotification(
                complaintId,
                complaint.getUserId(),
                "投诉已删除",
                "您的投诉已成功删除");
        System.out.println("=== 发送通知给居民 ===");
        System.out.println("居民ID: " + complaint.getUserId());
        System.out.println("通知发送结果: " + sent);
        System.out.println("通知内容: 您的投诉已成功删除");

        // 6. 删除投诉记录（由于设置了外键级联删除，相关的图片、位置、处理记录等会自动删除）
        boolean deleted = removeById(complaintId);
        if (!deleted) {
            throw new RuntimeException("删除投诉失败");
        }

        return true;
    }
}