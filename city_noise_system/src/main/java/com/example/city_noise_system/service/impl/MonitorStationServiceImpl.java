package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.entity.MonitorStation;
import com.example.city_noise_system.mapper.MonitorStationMapper;
import com.example.city_noise_system.service.MonitorStationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class MonitorStationServiceImpl extends ServiceImpl<MonitorStationMapper, MonitorStation>
        implements MonitorStationService {

    @Override
    public List<MonitorStation> getByDistrict(String district) {
        QueryWrapper<MonitorStation> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(district)) {
            queryWrapper.eq("district", district);
        }

        queryWrapper.eq("is_active", 1); // 只返回启用的监测点
        queryWrapper.orderByAsc("station_name");

        return list(queryWrapper);
    }

    @Override
    public List<MonitorStation> getAllActiveStations() {
        QueryWrapper<MonitorStation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", 1);
        queryWrapper.orderByAsc("district", "station_name");

        return list(queryWrapper);
    }

    @Override
    public MonitorStation getStationDetail(Long stationId) {
        MonitorStation station = getById(stationId);

        if (station == null) {
            throw new RuntimeException("监测点不存在");
        }

        return station;
    }
}