package com.example.city_noise_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.mapper.UserMapper;
import com.example.city_noise_system.service.ComplaintService;
import com.example.city_noise_system.service.HandlingRecordService;
import com.example.city_noise_system.service.UserService;
import com.example.city_noise_system.vo.ComplaintListItemVO;
import com.example.city_noise_system.vo.HandlingRecordVO;
import com.example.city_noise_system.vo.ResultVO;
import com.example.city_noise_system.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 管理员控制器
 * 处理管理员专用的接口，包括管理用户、分配任务等
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private HandlingRecordService handlingRecordService;

    /**
     * 获取所有用户列表
     * GET /api/admin/users
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/users")
    public ResultVO<?> getAllUsers(
            @RequestAttribute String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleFilter) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 获取用户列表，包括删除的用户
            // 使用自定义方法查询所有用户，包括已删除的
            List<User> users = userMapper.selectAllIncludingDeleted();

            // 3. 根据roleFilter过滤用户
            if (StringUtils.hasText(roleFilter)) {
                System.out.println("Filtering users by role: " + roleFilter);
                users = users.stream()
                        .filter(user -> {
                            System.out.println("User role: " + user.getRole());
                            return roleFilter.equals(user.getRole());
                        })
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("Filtered users count: " + users.size());
            }

            // 4. 转换为VO
            List<UserVO> userVOs = users.stream().map(user -> {
                UserVO vo = new UserVO();
                vo.setId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setRole(user.getRole());
                vo.setPhone(user.getPhone());
                vo.setAvatar(user.getAvatar());
                vo.setCreateTime(user.getCreateTime());
                vo.setDeleted(user.getDeleted());
                return vo;
            }).collect(java.util.stream.Collectors.toList());

            // 4. 创建分页结果
            Page<UserVO> resultPage = new Page<>();
            resultPage.setRecords(userVOs);
            resultPage.setTotal(users.size());
            resultPage.setCurrent(page);
            resultPage.setSize(size);

            return ResultVO.success(resultPage);

        } catch (Exception e) {
            return ResultVO.error(500, "获取用户列表失败");
        }
    }

    /**
     * 创建新用户
     * POST /api/admin/users
     * 需要JWT Token认证（管理员角色）
     */
    @PostMapping("/users")
    public ResultVO<?> createUser(
            @RequestAttribute String role,
            @RequestBody User user) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(user.getUsername())) {
                return ResultVO.error(400, "用户名不能为空");
            }

            if (!StringUtils.hasText(user.getPassword())) {
                return ResultVO.error(400, "密码不能为空");
            }

            if (!StringUtils.hasText(user.getRealName())) {
                return ResultVO.error(400, "真实姓名不能为空");
            }

            if (!StringUtils.hasText(user.getRole())) {
                return ResultVO.error(400, "角色不能为空");
            }

            // 3. 检查用户名是否已存在
            User existingUser = userService.getByUsername(user.getUsername());
            if (existingUser != null) {
                return ResultVO.error(400, "用户名已存在");
            }

            // 4. 对密码进行MD5加密
            String encryptedPassword = org.springframework.util.DigestUtils.md5DigestAsHex(
                    user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            user.setPassword(encryptedPassword);

            // 5. 设置默认头像
            if (!StringUtils.hasText(user.getAvatar())) {
                user.setAvatar("/uploads/default-avatar.png");
            }

            // 6. 保存用户
            System.out.println("保存的用户角色: " + user.getRole());
            // 直接使用MyBatis Plus的BaseMapper插入用户，绕过可能的拦截器
            boolean saved = userService.getBaseMapper().insert(user) > 0;
            if (!saved) {
                return ResultVO.error(500, "创建用户失败");
            }

            // 7. 返回用户信息
            UserVO userVO = new UserVO();
            org.springframework.beans.BeanUtils.copyProperties(user, userVO);
            return ResultVO.success(userVO, "用户创建成功");

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 编辑用户
     * PUT /api/admin/users/{id}
     * 需要JWT Token认证（管理员角色）
     */
    @PutMapping("/users/{id}")
    public ResultVO<?> updateUser(
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody User user) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(user.getRealName())) {
                return ResultVO.error(400, "真实姓名不能为空");
            }

            if (!StringUtils.hasText(user.getRole())) {
                return ResultVO.error(400, "角色不能为空");
            }

            // 3. 更新用户信息
            boolean success = userService.updateUserInfo(id, user);

            if (success) {
                return ResultVO.success("用户更新成功");
            } else {
                return ResultVO.error(500, "用户更新失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "用户更新失败");
        }
    }

    /**
     * 删除用户
     * DELETE /api/admin/users/{id}
     * 需要JWT Token认证（管理员角色）
     */
    @DeleteMapping("/users/{id}")
    public ResultVO<?> deleteUser(
            @RequestAttribute String role,
            @PathVariable Long id) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 删除用户（软删除）
            // 由于User实体类配置了@TableLogic注解，removeById会自动处理软删除
            boolean success = userService.removeById(id);

            if (success) {
                return ResultVO.success("用户删除成功");
            } else {
                return ResultVO.error(500, "用户删除失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "用户删除失败");
        }
    }

    /**
     * 获取所有投诉列表
     * GET /api/admin/complaints
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/complaints")
    public ResultVO<?> getAllComplaints(
            @RequestAttribute String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String noiseType) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 获取所有投诉
            Page<ComplaintListItemVO> complaints = complaintService.getAllComplaints(page, size, status, district,
                    noiseType);

            return ResultVO.success(complaints);

        } catch (Exception e) {
            return ResultVO.error(500, "获取投诉列表失败");
        }
    }

    /**
     * 分配投诉任务
     * POST /api/admin/complaint/{id}/assign
     * 需要JWT Token认证（管理员角色）
     */
    @PostMapping("/complaint/{id}/assign")
    public ResultVO<?> assignComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestParam Long workerId) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 参数校验
            if (workerId == null) {
                return ResultVO.error(400, "处理人员ID不能为空");
            }

            // 3. 分配任务
            boolean success = complaintService.assignComplaint(id, workerId, userId);

            if (success) {
                return ResultVO.success("任务分配成功");
            } else {
                return ResultVO.error(500, "任务分配失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "任务分配失败");
        }
    }

    /**
     * 关闭投诉
     * POST /api/admin/complaint/{id}/close
     * 需要JWT Token认证（管理员角色）
     */
    @PostMapping("/complaint/{id}/close")
    public ResultVO<?> closeComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 关闭投诉
            boolean success = complaintService.updateComplaintStatus(
                    id,
                    "CLOSED",
                    StringUtils.hasText(remark) ? remark : "管理员关闭投诉",
                    userId);

            if (success) {
                return ResultVO.success("投诉已关闭");
            } else {
                return ResultVO.error(500, "关闭投诉失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "关闭投诉失败");
        }
    }

    /**
     * 获取系统统计数据
     * GET /api/admin/statistics
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/statistics")
    public ResultVO<?> getStatistics(@RequestAttribute String role) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 使用MyBatis Plus进行真实的统计数据查询
            StatisticsResponse response = new StatisticsResponse();

            // 统计用户总数
            long totalUsers = userService.count();
            response.setTotalUsers((int) totalUsers);

            // 统计投诉总数（排除已删除的投诉，包含所有状态）
            long totalComplaints = complaintService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.city_noise_system.entity.Complaint>()
                            .eq("deleted", 0));
            response.setTotalComplaints((int) totalComplaints);

            // 统计待处理投诉数（排除已删除的投诉）
            long pendingComplaints = complaintService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.city_noise_system.entity.Complaint>()
                            .eq("status", "PENDING")
                            .eq("deleted", 0));
            response.setPendingComplaints((int) pendingComplaints);

            // 统计处理中投诉数（排除已删除的投诉）
            long processingComplaints = complaintService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.city_noise_system.entity.Complaint>()
                            .eq("status", "PROCESSING")
                            .eq("deleted", 0));
            response.setProcessingComplaints((int) processingComplaints);

            // 统计已解决投诉数（包括已解决和已关闭，排除已删除的投诉）
            long resolvedComplaints = complaintService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.city_noise_system.entity.Complaint>()
                            .in("status", "RESOLVED", "CLOSED")
                            .eq("deleted", 0));
            response.setResolvedComplaints((int) resolvedComplaints);

            return ResultVO.success(response);

        } catch (Exception e) {
            // 优化异常处理，提供更清晰的错误信息
            return ResultVO.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 任务监控接口
     * GET /api/admin/tasks/monitor
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/tasks/monitor")
    public ResultVO<?> monitorTasks(
            @RequestAttribute String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long workerId) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 获取任务监控数据
            Page<ComplaintListItemVO> tasks = complaintService.getAllComplaints(page, size, status, null, null,
                    workerId);

            return ResultVO.success(tasks);

        } catch (Exception e) {
            return ResultVO.error(500, "任务监控查询失败");
        }
    }

    /**
     * 修改任务进度
     * PUT /api/admin/task/{id}/progress
     * 需要JWT Token认证（管理员角色）
     */
    @PutMapping("/task/{id}/progress")
    public ResultVO<?> updateTaskProgress(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody TaskProgressUpdateRequest request) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(request.getStatus())) {
                return ResultVO.error(400, "状态参数不能为空");
            }

            if (!StringUtils.hasText(request.getRemark())) {
                return ResultVO.error(400, "修改原因不能为空");
            }

            // 3. 更新任务进度
            boolean success = complaintService.updateComplaintStatus(
                    id,
                    request.getStatus(),
                    request.getRemark(),
                    request.getWorkerId() != null ? request.getWorkerId() : userId);

            if (success) {
                return ResultVO.success("任务进度修改成功");
            } else {
                return ResultVO.error(500, "任务进度修改失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "任务进度修改失败");
        }
    }

    /**
     * 查询任务修改历史
     * GET /api/admin/task/{id}/history
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/task/{id}/history")
    public ResultVO<?> getTaskHistory(
            @RequestAttribute String role,
            @PathVariable Long id) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 获取任务修改历史
            List<HandlingRecordVO> history = handlingRecordService.getRecordsByComplaintId(id);

            return ResultVO.success(history);

        } catch (Exception e) {
            return ResultVO.error(500, "任务修改历史查询失败");
        }
    }

    /**
     * 按行政区统计投诉数量
     * GET /api/admin/complaints/district-stats
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/complaints/district-stats")
    public ResultVO<?> getComplaintsByDistrict(
            @RequestAttribute String role) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 按行政区统计投诉数量
            java.util.Map<String, Integer> districtStats = complaintService.getComplaintsCountByDistrict();

            return ResultVO.success(districtStats);

        } catch (Exception e) {
            return ResultVO.error(500, "获取行政区统计数据失败");
        }
    }

    /**
     * 按噪音类型统计投诉数量
     * GET /api/admin/complaints/noise-type-stats
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/complaints/noise-type-stats")
    public ResultVO<?> getComplaintsByNoiseType(
            @RequestAttribute String role) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 按噪音类型统计投诉数量
            java.util.Map<String, Integer> noiseTypeStats = complaintService.getComplaintsCountByNoiseType();

            return ResultVO.success(noiseTypeStats);

        } catch (Exception e) {
            return ResultVO.error(500, "获取噪音类型统计数据失败");
        }
    }

    /**
     * 获取所有处理人员列表
     * GET /api/admin/workers
     * 需要JWT Token认证（管理员角色）
     */
    @GetMapping("/workers")
    public ResultVO<?> getWorkers(@RequestAttribute String role) {
        try {
            // 1. 检查用户角色
            if (!"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限访问此接口");
            }

            // 2. 获取所有WORKER角色的用户
            List<User> workers = userService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                            .eq("role", "WORKER"));

            // 3. 转换为VO
            List<UserVO> workerVOs = workers.stream().map(user -> {
                UserVO vo = new UserVO();
                vo.setId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setRole(user.getRole());
                vo.setPhone(user.getPhone());
                vo.setAvatar(user.getAvatar());
                vo.setCreateTime(user.getCreateTime());
                vo.setDeleted(user.getDeleted());
                return vo;
            }).collect(java.util.stream.Collectors.toList());

            return ResultVO.success(workerVOs);

        } catch (Exception e) {
            return ResultVO.error(500, "获取处理人员列表失败");
        }
    }

    // ==================== 内部响应类 ====================

    /**
     * 统计数据响应类
     */
    public static class StatisticsResponse {
        private int totalUsers;
        private int totalComplaints;
        private int pendingComplaints;
        private int processingComplaints;
        private int resolvedComplaints;

        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getTotalComplaints() {
            return totalComplaints;
        }

        public void setTotalComplaints(int totalComplaints) {
            this.totalComplaints = totalComplaints;
        }

        public int getPendingComplaints() {
            return pendingComplaints;
        }

        public void setPendingComplaints(int pendingComplaints) {
            this.pendingComplaints = pendingComplaints;
        }

        public int getProcessingComplaints() {
            return processingComplaints;
        }

        public void setProcessingComplaints(int processingComplaints) {
            this.processingComplaints = processingComplaints;
        }

        public int getResolvedComplaints() {
            return resolvedComplaints;
        }

        public void setResolvedComplaints(int resolvedComplaints) {
            this.resolvedComplaints = resolvedComplaints;
        }
    }

    /**
     * 任务进度更新请求类
     */
    public static class TaskProgressUpdateRequest {
        private String status;
        private String remark;
        private Long workerId;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Long getWorkerId() {
            return workerId;
        }

        public void setWorkerId(Long workerId) {
            this.workerId = workerId;
        }
    }
}