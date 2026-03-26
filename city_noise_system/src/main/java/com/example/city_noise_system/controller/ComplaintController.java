package com.example.city_noise_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city_noise_system.dto.ComplaintCreateDTO;
import com.example.city_noise_system.dto.RatingSubmitDTO;
import com.example.city_noise_system.dto.UnsolvedSubmitDTO;
import com.example.city_noise_system.entity.Complaint;
import com.example.city_noise_system.service.ComplaintService;
import com.example.city_noise_system.service.RatingService;
import com.example.city_noise_system.service.ComplaintImageService;
import com.example.city_noise_system.utils.FileUploadUtil;
import com.example.city_noise_system.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;

/**
 * 投诉控制器
 * 处理投诉相关的接口，包括提交、查询、更新等
 */
@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private ComplaintImageService complaintImageService;

    /**
     * 提交投诉（支持文件上传）
     * POST /api/complaint
     * 需要JWT Token认证（居民角色）
     */
    @PostMapping(consumes = { "multipart/form-data" })
    public ResultVO<?> createComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "noiseType", required = false) String noiseType,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "detailAddress", required = false) String detailAddress,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            // 1. 检查用户角色（只有居民和管理员可以提交投诉）
            if (!"RESIDENT".equals(role) && !"ADMIN".equals(role)) {
                return ResultVO.error(403, "无权限提交投诉");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(title)) {
                return ResultVO.error(400, "投诉标题不能为空");
            }

            if (!StringUtils.hasText(description)) {
                return ResultVO.error(400, "投诉描述不能为空");
            }

            if (longitude == null || latitude == null) {
                return ResultVO.error(400, "位置信息不能为空");
            }

            if (!StringUtils.hasText(district)) {
                return ResultVO.error(400, "行政区不能为空");
            }

            if (!StringUtils.hasText(detailAddress)) {
                return ResultVO.error(400, "详细地址不能为空");
            }

            // 3. 创建 ComplaintCreateDTO
            ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
            createDTO.setTitle(title);
            createDTO.setDescription(description);
            createDTO.setNoiseType(noiseType);
            createDTO.setLongitude(longitude);
            createDTO.setLatitude(latitude);
            createDTO.setDistrict(district);
            createDTO.setDetailAddress(detailAddress);

            // 3. 文件上传校验
            if (images != null && !images.isEmpty()) {
                // 检查文件数量
                if (images.size() > 5) {
                    return ResultVO.error(400, "最多只能上传5个文件");
                }

                // 检查每个文件
                for (MultipartFile file : images) {
                    if (file.isEmpty()) {
                        return ResultVO.error(400, "文件不能为空");
                    }

                    // 检查文件大小（5MB）
                    if (file.getSize() > 5 * 1024 * 1024) {
                        return ResultVO.error(400, "文件大小不能超过5MB");
                    }

                    // 检查文件类型
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResultVO.error(400, "只支持图片文件");
                    }

                    // 检查具体的图片类型
                    String[] allowedTypes = { "image/jpeg", "image/png", "image/gif", "image/webp" };
                    boolean isAllowed = false;
                    for (String type : allowedTypes) {
                        if (type.equals(contentType)) {
                            isAllowed = true;
                            break;
                        }
                    }
                    if (!isAllowed) {
                        return ResultVO.error(400, "只支持JPG、PNG、GIF、WEBP格式的图片");
                    }
                }
            }

            // 4. 创建投诉
            Long complaintId = complaintService.createComplaint(createDTO, userId, images);

            // 5. 构建响应数据
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("id", complaintId);

            // 如果有上传文件，获取文件路径
            if (images != null && !images.isEmpty()) {
                // 创建投诉后查询图片路径
                java.util.List<String> uploadedImages = complaintService.getComplaintImages(complaintId);
                responseData.put("images", uploadedImages);
            }

            return ResultVO.success(responseData, "投诉提交成功");

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "提交投诉失败，请稍后重试");
        }
    }

    /**
     * 提交投诉（兼容JSON格式，不带文件）
     * POST /api/complaint
     * 需要JWT Token认证（居民角色）
     */
    @PostMapping(consumes = "application/json")
    public ResultVO<?> createComplaintJson(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @RequestBody ComplaintCreateDTO createDTO) {
        // 从 createDTO 中提取参数，调用新的 createComplaint 方法
        return createComplaint(
                userId,
                role,
                createDTO.getTitle(),
                createDTO.getDescription(),
                createDTO.getNoiseType(),
                createDTO.getLongitude(),
                createDTO.getLatitude(),
                createDTO.getDistrict(),
                createDTO.getDetailAddress(),
                null);
    }

    /**
     * 获取我的投诉列表
     * GET /api/complaint/my
     * 需要JWT Token认证
     */
    @GetMapping("/my")
    public ResultVO<?> getMyComplaints(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        try {
            Page<ComplaintListItemVO> result = complaintService.getMyComplaints(userId, page, size, status);

            return ResultVO.success(result);

        } catch (Exception e) {
            return ResultVO.error(500, "获取投诉列表失败");
        }
    }

    /**
     * 获取投诉详情
     * GET /api/complaint/{id}
     * 需要JWT Token认证
     */
    @GetMapping("/{id}")
    public ResultVO<?> getComplaintDetail(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        try {
            ComplaintDetailVO detail = complaintService.getComplaintDetail(id, userId);
            return ResultVO.success(detail);
        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "获取投诉详情失败");
        }
    }

    /**
     * 获取地图展示的投诉点
     * GET /api/complaint/map-points
     * 需要JWT Token认证
     */
    @GetMapping("/map-points")
    public ResultVO<?> getComplaintMapPoints() {
        try {
            List<ComplaintMapPointVO> mapPoints = complaintService.getComplaintMapPoints();
            return ResultVO.success(mapPoints);
        } catch (Exception e) {
            return ResultVO.error(500, "获取地图数据失败");
        }
    }

    /**
     * 上传投诉图片
     * POST /api/complaint/{id}/upload-image
     * 需要JWT Token认证
     * 注意：这个接口可以集成到createComplaint中，这里单独提供
     */
    @PostMapping("/{id}/upload-image")
    public ResultVO<?> uploadComplaintImage(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            // 1. 检查投诉状态（只有待处理和已分配状态可以上传图片）
            Complaint complaint = complaintService.getById(id);
            if (complaint == null) {
                return ResultVO.error(404, "投诉不存在");
            }

            String status = complaint.getStatus();
            if (!"PENDING".equals(status) && !"ASSIGNED".equals(status)) {
                return ResultVO.error(400, "当前投诉状态不允许上传图片");
            }

            // 2. 检查文件是否为空
            if (file.isEmpty()) {
                return ResultVO.error(400, "文件不能为空");
            }

            // 3. 检查文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResultVO.error(400, "文件大小不能超过5MB");
            }

            // 4. 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResultVO.error(400, "只支持图片文件");
            }

            // 5. 检查具体的图片类型
            String[] allowedTypes = { "image/jpeg", "image/png", "image/gif", "image/webp" };
            boolean isAllowed = false;
            for (String type : allowedTypes) {
                if (type.equals(contentType)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                return ResultVO.error(400, "只支持JPG、PNG、GIF、WEBP格式的图片");
            }

            // 6. 上传图片
            String imageUrl = fileUploadUtil.uploadImage(file);

            // 7. 保存图片信息到数据库
            boolean saved = complaintImageService.saveImage(id, imageUrl);
            if (!saved) {
                return ResultVO.error(500, "保存图片信息失败");
            }

            // 8. 返回结果
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("imageUrl", imageUrl);
            return ResultVO.success(responseData, "图片上传成功");
        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 评价投诉
     * POST /api/complaint/{id}/rating
     * 需要JWT Token认证（居民角色）
     */
    @PostMapping("/{id}/rating")
    public ResultVO<?> rateComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody RatingSubmitDTO ratingDTO) {
        try {
            // 1. 检查用户角色
            if (!"RESIDENT".equals(role)) {
                return ResultVO.error(403, "只有居民可以评价投诉");
            }

            // 2. 参数校验
            if (ratingDTO.getScore() == null || ratingDTO.getScore() < 1 || ratingDTO.getScore() > 5) {
                return ResultVO.error(400, "评分必须在1-5分之间");
            }

            if (!StringUtils.hasText(ratingDTO.getComment())) {
                return ResultVO.error(400, "评价内容不能为空");
            }

            // 3. 调用评价服务
            boolean success = ratingService.addRating(
                    id,
                    userId,
                    ratingDTO.getScore(),
                    ratingDTO.getComment());

            if (success) {
                return ResultVO.success("评价成功");
            } else {
                return ResultVO.error(500, "评价失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "评价失败: " + e.getMessage());
        }
    }

    /**
     * 获取附近的投诉点
     * GET /api/complaint/nearby
     * 需要JWT Token认证
     */
    @GetMapping("/nearby")
    public ResultVO<?> getNearbyComplaints(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam int radius,
            @RequestParam(required = false) String noiseType) {
        try {
            List<ComplaintMapPointVO> nearbyPoints = complaintService.getNearbyComplaints(longitude, latitude, radius,
                    noiseType);
            return ResultVO.success(nearbyPoints);
        } catch (Exception e) {
            return ResultVO.error(500, "获取附近投诉点失败: " + e.getMessage());
        }
    }

    /**
     * 标记投诉为未解决
     * POST /api/complaint/{id}/unsolved
     * 需要JWT Token认证（居民角色）
     */
    @PostMapping("/{id}/unsolved")
    public ResultVO<?> markAsUnsolved(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody UnsolvedSubmitDTO unsolvedDTO) {
        try {
            // 1. 检查用户角色
            if (!"RESIDENT".equals(role)) {
                return ResultVO.error(403, "只有居民可以标记投诉为未解决");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(unsolvedDTO.getReason())) {
                return ResultVO.error(400, "问题原因不能为空");
            }

            // 3. 调用服务标记为未解决
            boolean success = complaintService.markAsUnsolved(
                    id,
                    userId,
                    unsolvedDTO.getReason());

            if (success) {
                return ResultVO.success("已标记为未解决，处理人员将重新处理");
            } else {
                return ResultVO.error(500, "标记未解决失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "标记未解决失败: " + e.getMessage());
        }
    }

    /**
     * 删除投诉
     * POST /api/complaint/{id}/delete
     * 需要JWT Token认证（居民或管理员角色）
     */
    @PostMapping("/{id}/delete")
    public ResultVO<?> deleteComplaint(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @PathVariable Long id,
            @RequestBody UnsolvedSubmitDTO deleteDTO) {
        try {
            // 1. 检查用户角色
            if (!"RESIDENT".equals(role) && !"ADMIN".equals(role)) {
                return ResultVO.error(403, "只有居民和管理员可以删除投诉");
            }

            // 2. 参数校验
            if (!StringUtils.hasText(deleteDTO.getReason())) {
                return ResultVO.error(400, "删除原因不能为空");
            }

            // 3. 调用服务删除投诉
            boolean success = complaintService.deleteComplaint(
                    id,
                    userId,
                    deleteDTO.getReason(),
                    role);

            if (success) {
                return ResultVO.success("投诉已成功删除");
            } else {
                return ResultVO.error(500, "删除投诉失败");
            }

        } catch (RuntimeException e) {
            return ResultVO.error(400, e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(500, "删除投诉失败: " + e.getMessage());
        }
    }
}