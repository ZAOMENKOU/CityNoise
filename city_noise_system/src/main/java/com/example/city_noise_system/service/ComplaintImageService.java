package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.ComplaintImage;
import java.util.List;

public interface ComplaintImageService extends IService<ComplaintImage> {
    boolean saveImage(Long complaintId, String imageUrl);
    List<String> getImagesByComplaintId(Long complaintId);
}