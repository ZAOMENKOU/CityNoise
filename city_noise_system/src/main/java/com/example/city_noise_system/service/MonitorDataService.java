package com.example.city_noise_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.city_noise_system.entity.MonitorData;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 监测数据服务接口
 * 管理环境噪声的监测数据
 */
public interface MonitorDataService extends IService<MonitorData> {

    /**
     * 添加监测数据
     */
    boolean addMonitorData(Long stationId, BigDecimal noiseLevel, Date monitorTime);

    /**
     * 获取监测点的最新数据
     */
    MonitorData getLatestData(Long stationId);

    /**
     * 获取监测点的历史数据（按时间范围）
     */
    List<MonitorData> getHistoricalData(Long stationId, Date startTime, Date endTime);

    /**
     * 获取各监测点今日的平均噪声值
     */
    Map<Long, BigDecimal> getTodayAverageNoise();

    /**
     * 判断监测数据是否超标
     */
    boolean isExceedStandard(Long stationId, BigDecimal noiseLevel, String periodType);

    /**
     * 生成模拟监测数据（用于演示和测试）
     */
    void generateSimulatedData();
}