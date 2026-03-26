package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.MonitorStation;
import java.util.List;

/**
 * 监测点服务接口
 * 管理环境噪声监测点的信息
 */
public interface MonitorStationService extends IService<MonitorStation> {

    /**
     * 根据行政区获取监测点列表
     */
    List<MonitorStation> getByDistrict(String district);

    /**
     * 获取所有启用的监测点
     */
    List<MonitorStation> getAllActiveStations();

    /**
     * 根据监测点ID获取详细信息
     */
    MonitorStation getStationDetail(Long stationId);
}