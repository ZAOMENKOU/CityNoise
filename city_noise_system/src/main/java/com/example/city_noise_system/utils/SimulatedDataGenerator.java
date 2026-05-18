package com.example.city_noise_system.utils;

import com.example.city_noise_system.entity.MonitorData;
import com.example.city_noise_system.entity.MonitorStation;
import com.example.city_noise_system.entity.NoiseStandard;
import com.example.city_noise_system.mapper.MonitorDataMapper;
import com.example.city_noise_system.mapper.MonitorStationMapper;
import com.example.city_noise_system.mapper.NoiseStandardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulatedDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(SimulatedDataGenerator.class);

    @Autowired
    private MonitorStationMapper monitorStationMapper;

    @Autowired
    private NoiseStandardMapper noiseStandardMapper;

    @Autowired
    private MonitorDataMapper monitorDataMapper;

    private Map<String, NoiseStandard> standardCache = new ConcurrentHashMap<>();

    public void generateSimulatedData() {
        log.info("开始生成当前时间模拟数据...");

        clearSimulatedData();

        initStandardCache();

        List<MonitorStation> stations = monitorStationMapper.selectList(null);
        if (stations.isEmpty()) {
            log.warn("未找到监测点，无法生成模拟数据");
            return;
        }

        log.info("找到 {} 个监测点", stations.size());

        Date currentTime = new Date();
        log.info("生成数据时间: {}", currentTime);

        List<MonitorData> batchData = new ArrayList<>(stations.size());

        for (MonitorStation station : stations) {
            BigDecimal noiseLevel = generateNoiseLevel(station, currentTime);
            MonitorData data = createMonitorData(station, noiseLevel, currentTime);
            batchData.add(data);
        }

        if (!batchData.isEmpty()) {
            saveBatchData(batchData);
        }

        log.info("当前时间模拟数据生成完成，共生成 {} 条数据", batchData.size());
    }

    private void initStandardCache() {
        standardCache.clear();
        List<NoiseStandard> standards = noiseStandardMapper.selectList(null);
        for (NoiseStandard standard : standards) {
            standardCache.put(standard.getFunctionalZone(), standard);
        }
        log.info("噪声标准缓存初始化完成，共加载 {} 个功能区标准", standardCache.size());
    }

    public BigDecimal generateNoiseLevel(MonitorStation station, Date monitorTime) {
        String functionalZone = station.getFunctionalZone();
        NoiseStandard standard = standardCache.get(functionalZone);
        if (standard == null) {
            log.warn("未找到功能区 {} 的噪声标准，使用默认值50dB", functionalZone);
            return BigDecimal.valueOf(50.0);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monitorTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isDayTime = hour >= 6 && hour < 22;

        BigDecimal baseLimit = isDayTime ? standard.getDayLimit() : standard.getNightLimit();
        double randomBase = baseLimit.doubleValue() + (Math.random() * 5 - 2);

        double timeFactor = 0;
        if (isDayTime) {
            boolean isWeekday = dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
            if (isWeekday) {
                if ((hour >= 7 && hour < 9) || (hour >= 17 && hour < 19)) {
                    timeFactor = 3 + Math.random() * 3;
                }
            } else {
                timeFactor = 1 + Math.random() * 2;
            }
        } else {
            if (hour >= 0 && hour < 6) {
                timeFactor = -5 - Math.random() * 3;
            } else {
                timeFactor = 1 + Math.random() * 2;
            }
        }

        double zoneFactor = 0;
        switch (functionalZone) {
            case "CLASS_1":
                zoneFactor = Math.random() * 2 - 1;
                break;
            case "CLASS_2":
                zoneFactor = Math.random() * 3 - 1.5;
                break;
            case "CLASS_3":
                zoneFactor = 2 + Math.random() * 3;
                if (!isDayTime) {
                    zoneFactor -= 3;
                }
                break;
            case "CLASS_4A":
            case "CLASS_4B":
                zoneFactor = 3 + Math.random() * 4;
                break;
        }

        double finalValue = randomBase + timeFactor + zoneFactor;
        finalValue = Math.max(30, Math.min(90, finalValue));

        return BigDecimal.valueOf(finalValue).setScale(2, RoundingMode.HALF_UP);
    }

    private MonitorData createMonitorData(MonitorStation station, BigDecimal noiseLevel, Date monitorTime) {
        MonitorData data = new MonitorData();
        data.setStationId(station.getId());
        data.setNoiseLevel(noiseLevel);
        data.setMonitorTime(monitorTime);
        data.setDataSource("SIMULATED");
        return data;
    }

    private void saveBatchData(List<MonitorData> dataList) {
        if (dataList.isEmpty()) {
            return;
        }

        try {
            for (MonitorData data : dataList) {
                monitorDataMapper.insert(data);
            }
            log.info("批量保存 {} 条监测数据成功", dataList.size());
        } catch (Exception e) {
            log.error("批量保存监测数据失败", e);
        }
    }

    public void clearSimulatedData() {
        try {
            monitorDataMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                    .eq("data_source", "SIMULATED"));
            log.info("成功清空所有历史模拟数据");
        } catch (Exception e) {
            log.error("清空模拟数据失败", e);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void generateRealTimeSimulatedData() {
        log.info("定时任务触发生成当前时间模拟数据...");
        generateSimulatedData();
    }
}
