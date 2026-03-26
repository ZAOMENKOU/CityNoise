package com.example.city_noise_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city_noise_system.dto.TaskUpdateDTO;
import com.example.city_noise_system.entity.HandlingRecord;
import com.example.city_noise_system.entity.Complaint;
import com.example.city_noise_system.entity.ComplaintLocation;
import com.example.city_noise_system.service.ComplaintService;
import com.example.city_noise_system.service.HandlingRecordService;
import com.example.city_noise_system.service.ComplaintLocationService;
import com.example.city_noise_system.vo.ComplaintListItemVO;
import com.example.city_noise_system.vo.HandlingRecordVO;
import com.example.city_noise_system.vo.ResultVO;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 处理人员控制器
 * 处理处理人员相关的接口，包括获取任务列表、处理投诉等
 */
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private HandlingRecordService handlingRecordService;

    @Autowired
    private ComplaintLocationService complaintLocationService;

    /**
     * 获取我的任务列表
     * GET /api/worker/tasks
     * 需要JWT Token认证（处理人员角色）
     */
    @GetMapping("/tasks")
    public ResultVO<?> getMyTasks(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        try {
            // 1. 检查用户角色
            if (!"WORKER".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 使用优化后的数据库查询方法获取任务列表
            // 直接从数据库查询，替代内存过滤，提高性能
            Page<ComplaintListItemVO> tasks = complaintService.getWorkerTasks(
                    userId, page, size, status);

            return ResultVO.success(tasks);

        } catch (Exception e) {
            // 优化异常处理，提供更清晰的错误信息
            return ResultVO.error(500, "获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 开始处理投诉
     * POST /api/worker/task/{id}/start
     * 需要JWT Token认证（处理人员角色）
     */
    @PostMapping("/task/{id}/start")
    public ResultVO<?> startProcessComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        try {
            // 1. 检查用户角色
            if (!"WORKER".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 开始处理投诉
            boolean success = complaintService.processComplaint(
                    id,
                    StringUtils.hasText(remark) ? remark : "开始处理投诉",
                    userId);

            if (success) {
                return ResultVO.success("已开始处理投诉");
            } else {
                return ResultVO.error(500, "开始处理投诉失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "开始处理投诉失败");
        }
    }

    /**
     * 更新处理进展
     * POST /api/worker/task/{id}/update
     * 需要JWT Token认证（处理人员角色）
     */
    @PostMapping("/task/{id}/update")
    public ResultVO<?> updateComplaintProgress(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody TaskUpdateDTO updateDTO) {
        try {
            // 1. 检查用户角色
            if (!"WORKER".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 参数校验
            String remark = updateDTO.getRemark();
            if (!StringUtils.hasText(remark)) {
                return ResultVO.error(400, "处理备注不能为空");
            }

            // 3. 更新处理进展
            boolean success = complaintService.updateComplaintStatus(
                    id, "PROCESSING", remark, userId);

            if (success) {
                return ResultVO.success("处理进展已更新");
            } else {
                return ResultVO.error(500, "更新处理进展失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "更新处理进展失败");
        }
    }

    /**
     * 完成投诉处理
     * POST /api/worker/task/{id}/resolve
     * 需要JWT Token认证（处理人员角色）
     */
    @PostMapping("/task/{id}/resolve")
    public ResultVO<?> resolveComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        try {
            // 1. 检查用户角色
            if (!"WORKER".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 完成投诉处理
            boolean success = complaintService.resolveComplaint(
                    id,
                    StringUtils.hasText(remark) ? remark : "投诉处理完成",
                    userId);

            if (success) {
                return ResultVO.success("投诉处理完成");
            } else {
                return ResultVO.error(500, "完成投诉处理失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "完成投诉处理失败");
        }
    }

    /**
     * 获取处理记录
     * GET /api/worker/task/{id}/records
     * 需要JWT Token认证
     */
    @GetMapping("/task/{id}/records")
    public ResultVO<?> getHandlingRecords(
            @PathVariable Long id) {
        try {
            List<HandlingRecordVO> records = handlingRecordService.getRecordsByComplaintId(id);
            return ResultVO.success(records);
        } catch (Exception e) {
            return ResultVO.error(500, "获取处理记录失败");
        }
    }

    /**
     * 获取处理历史
     * POST /api/worker/history
     * 需要JWT Token认证
     */
    @PostMapping("/history")
    public ResultVO<?> getProcessingHistory(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @RequestBody ProcessingHistoryRequest request) {
        try {
            // 1. 检查用户角色
            if (!"WORKER".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 构建查询条件
            QueryWrapper<HandlingRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("handler_id", userId);

            // 3. 根据状态筛选
            if (StringUtils.hasText(request.getStatus())) {
                if ("COMPLETED".equals(request.getStatus())) {
                    // 已完成状态对应RESOLVE操作
                    queryWrapper.eq("action", "RESOLVE");
                } else if ("PROCESSED".equals(request.getStatus())) {
                    // 已处理状态对应非RESOLVE操作
                    queryWrapper.ne("action", "RESOLVE");
                }
            }

            // 4. 根据时间范围筛选
            if (StringUtils.hasText(request.getStartDate())) {
                queryWrapper.ge("create_time", request.getStartDate());
            }
            if (StringUtils.hasText(request.getEndDate())) {
                queryWrapper.le("create_time", request.getEndDate() + " 23:59:59");
            }

            // 5. 按处理时间倒序排序
            queryWrapper.orderByDesc("create_time");

            // 6. 执行查询
            List<HandlingRecord> records = handlingRecordService.list(queryWrapper);

            // 7. 转换为VO并添加投诉信息
            List<ProcessingHistoryVO> historyVOs = records.stream().map(record -> {
                ProcessingHistoryVO vo = new ProcessingHistoryVO();
                vo.setId(record.getId());
                vo.setComplaintId(record.getComplaintId());

                // 获取投诉信息
                Complaint complaint = complaintService.getById(record.getComplaintId());
                if (complaint != null) {
                    vo.setComplaintTitle(complaint.getTitle());
                    vo.setNoiseType(complaint.getNoiseType());

                    // 获取投诉位置信息
                    ComplaintLocation location = complaintLocationService.getByComplaintId(complaint.getId());
                    if (location != null) {
                        vo.setDistrict(location.getDistrict());
                    }
                }

                vo.setProcessTime(record.getCreateTime());
                // 根据操作类型设置状态
                if ("RESOLVE".equals(record.getAction())) {
                    vo.setStatus("COMPLETED");
                } else {
                    vo.setStatus("PROCESSED");
                }
                vo.setResult(record.getRemark());

                return vo;
            }).collect(java.util.stream.Collectors.toList());

            // 8. 构建分页结果
            int start = (request.getPage() - 1) * request.getPageSize();
            int end = Math.min(start + request.getPageSize(), historyVOs.size());
            List<ProcessingHistoryVO> pageRecords = historyVOs.subList(start, end);

            Page<ProcessingHistoryVO> page = new Page<>();
            page.setRecords(pageRecords);
            page.setTotal(historyVOs.size());
            page.setCurrent(request.getPage());
            page.setSize(request.getPageSize());

            return ResultVO.success(page);

        } catch (Exception e) {
            return ResultVO.error(500, "获取处理历史失败");
        }
    }

    // ==================== 内部响应类 ====================

    /**
     * 处理历史请求类
     */
    public static class ProcessingHistoryRequest {
        private String status;
        private String startDate;
        private String endDate;
        private int page;
        private int pageSize;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }

    /**
     * 处理历史VO类
     */
    public static class ProcessingHistoryVO {
        private Long id;
        private Long complaintId;
        private String complaintTitle;
        private String district;
        private String noiseType;
        private Date processTime;
        private String status;
        private String result;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getComplaintId() {
            return complaintId;
        }

        public void setComplaintId(Long complaintId) {
            this.complaintId = complaintId;
        }

        public String getComplaintTitle() {
            return complaintTitle;
        }

        public void setComplaintTitle(String complaintTitle) {
            this.complaintTitle = complaintTitle;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getNoiseType() {
            return noiseType;
        }

        public void setNoiseType(String noiseType) {
            this.noiseType = noiseType;
        }

        public Date getProcessTime() {
            return processTime;
        }

        public void setProcessTime(Date processTime) {
            this.processTime = processTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}