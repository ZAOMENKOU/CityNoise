package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.entity.MonitorData;
import com.example.city_noise_system.entity.MonitorStation;
import com.example.city_noise_system.entity.NoiseStandard;
import com.example.city_noise_system.mapper.MonitorDataMapper;
import com.example.city_noise_system.mapper.MonitorStationMapper;
import com.example.city_noise_system.mapper.NoiseStandardMapper;
import com.example.city_noise_system.service.MonitorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MonitorDataServiceImpl extends ServiceImpl<MonitorDataMapper, MonitorData>
        implements MonitorDataService {

    @Autowired
    private MonitorStationMapper monitorStationMapper;

    @Autowired
    private NoiseStandardMapper noiseStandardMapper;

    @Override
    public boolean addMonitorData(Long stationId, BigDecimal noiseLevel, Date monitorTime) {
        // 检查监测点是否存在
        MonitorStation station = monitorStationMapper.selectById(stationId);
        if (station == null) {
            throw new RuntimeException("监测点不存在");
        }

        // 创建监测数据记录
        MonitorData data = new MonitorData();
        data.setStationId(stationId);
        data.setNoiseLevel(noiseLevel);
        data.setMonitorTime(monitorTime);

        // 根据监测时间判断时段（6:00-22:00为昼间，其他为夜间）
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monitorTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 22) {
            data.setPeriodType("DAY");
        } else {
            data.setPeriodType("NIGHT");
        }

        return save(data);
    }

    @Override
    public MonitorData getLatestData(Long stationId) {
        QueryWrapper<MonitorData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("station_id", stationId);
        queryWrapper.orderByDesc("monitor_time");
        queryWrapper.last("LIMIT 1");

        return getOne(queryWrapper);
    }

    @Override
    public List<MonitorData> getHistoricalData(Long stationId, Date startTime, Date endTime) {
        QueryWrapper<MonitorData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("station_id", stationId);

        if (startTime != null) {
            queryWrapper.ge("monitor_time", startTime);
        }

        if (endTime != null) {
            queryWrapper.le("monitor_time", endTime);
        }

        queryWrapper.orderByAsc("monitor_time");

        return list(queryWrapper);
    }

    @Override
    public Map<Long, BigDecimal> getTodayAverageNoise() {
        // 获取今天0点的时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        // 获取所有监测点
        List<MonitorStation> stations = monitorStationMapper.selectList(null);
        Map<Long, BigDecimal> result = new HashMap<>();

        for (MonitorStation station : stations) {
            QueryWrapper<MonitorData> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("station_id", station.getId());
            queryWrapper.ge("monitor_time", todayStart);
            queryWrapper.select("AVG(noise_level) as avg_noise");

            List<Map<String, Object>> maps = listMaps(queryWrapper);
            if (!maps.isEmpty() && maps.get(0).get("avg_noise") != null) {
                BigDecimal avgNoise = (BigDecimal) maps.get(0).get("avg_noise");
                result.put(station.getId(), avgNoise.setScale(2, RoundingMode.HALF_UP));
            } else {
                result.put(station.getId(), BigDecimal.ZERO);
            }
        }

        return result;
    }

    @Override
    public boolean isExceedStandard(Long stationId, BigDecimal noiseLevel, String periodType) {
        // 获取监测点信息
        MonitorStation station = monitorStationMapper.selectById(stationId);
        if (station == null) {
            throw new RuntimeException("监测点不存在");
        }

        // 获取该功能区的噪声标准
        QueryWrapper<NoiseStandard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("functional_zone", station.getFunctionalZone());
        NoiseStandard standard = noiseStandardMapper.selectOne(queryWrapper);

        if (standard == null) {
            throw new RuntimeException("未找到该功能区的噪声标准");
        }

        // 根据时段判断是否超标
        if ("DAY".equals(periodType)) {
            return noiseLevel.compareTo(standard.getDayLimit()) > 0;
        } else if ("NIGHT".equals(periodType)) {
            return noiseLevel.compareTo(standard.getNightLimit()) > 0;
        } else {
            throw new RuntimeException("时段类型不正确，应该是DAY或NIGHT");
        }
    }

    @Autowired
    private com.example.city_noise_system.utils.SimulatedDataGenerator simulatedDataGenerator;

    @Override
    public void generateSimulatedData() {
        // 使用新的模拟数据生成器
        simulatedDataGenerator.generateSimulatedData();
    }
}