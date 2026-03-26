package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.ComplaintLocation;
import com.example.city_noise_system.dto.LocationDTO;
import java.util.List;

/**
 * 投诉位置服务接口
 */
public interface ComplaintLocationService extends IService<ComplaintLocation> {

    /**
     * 保存投诉位置
     */
    boolean saveComplaintLocation(Long complaintId, LocationDTO locationDTO);

    /**
     * 根据投诉ID获取位置
     */
    ComplaintLocation getByComplaintId(Long complaintId);

    /**
     * 根据行政区获取投诉位置列表
     */
    List<ComplaintLocation> getByDistrict(String district);

    /**
     * 获取所有投诉位置（用于地图）
     */
    List<ComplaintLocation> getAllForMap();
}