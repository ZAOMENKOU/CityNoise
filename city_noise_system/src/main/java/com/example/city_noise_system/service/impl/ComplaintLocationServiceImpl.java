package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.dto.LocationDTO;
import com.example.city_noise_system.entity.ComplaintLocation;
import com.example.city_noise_system.mapper.ComplaintLocationMapper;
import com.example.city_noise_system.service.ComplaintLocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class ComplaintLocationServiceImpl extends ServiceImpl<ComplaintLocationMapper, ComplaintLocation>
        implements ComplaintLocationService {

    @Override
    public boolean saveComplaintLocation(Long complaintId, LocationDTO locationDTO) {
        ComplaintLocation location = new ComplaintLocation();
        BeanUtils.copyProperties(locationDTO, location);
        location.setComplaintId(complaintId);

        return save(location);
    }

    @Override
    public ComplaintLocation getByComplaintId(Long complaintId) {
        QueryWrapper<ComplaintLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complaint_id", complaintId);
        return getOne(queryWrapper);
    }

    @Override
    public List<ComplaintLocation> getByDistrict(String district) {
        QueryWrapper<ComplaintLocation> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(district)) {
            queryWrapper.eq("district", district);
        }
        return list(queryWrapper);
    }

    @Override
    public List<ComplaintLocation> getAllForMap() {
        QueryWrapper<ComplaintLocation> queryWrapper = new QueryWrapper<>();
        return list(queryWrapper);
    }
}