package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.entity.ComplaintImage;
import com.example.city_noise_system.mapper.ComplaintImageMapper;
import com.example.city_noise_system.service.ComplaintImageService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintImageServiceImpl extends ServiceImpl<ComplaintImageMapper, ComplaintImage>
        implements ComplaintImageService {

    @Override
    public boolean saveImage(Long complaintId, String imageUrl) {
        ComplaintImage image = new ComplaintImage();
        image.setComplaintId(complaintId);
        image.setImageUrl(imageUrl);
        return save(image);
    }

    @Override
    public List<String> getImagesByComplaintId(Long complaintId) {
        QueryWrapper<ComplaintImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complaint_id", complaintId);
        queryWrapper.orderByAsc("create_time");

        List<ComplaintImage> images = list(queryWrapper);
        return images.stream()
                .map(ComplaintImage::getImageUrl)
                .collect(Collectors.toList());
    }
}