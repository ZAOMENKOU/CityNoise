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

/**
 * 模拟数据生成器
 * 基于轻量级噪音监测数据生成方案（方案A）实现
 */
@Component
public class SimulatedDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(SimulatedDataGenerator.class);

    @Autowired
    private MonitorStationMapper monitorStationMapper;

    @Autowired
    private NoiseStandardMapper noiseStandardMapper;

    @Autowired
    private MonitorDataMapper monitorDataMapper;

    // 配置参数
    private int days = 7; // 生成最近7天的数据
    private int hoursPerDay = 24; // 每天24小时
    private int batchSize = 1000; // 每批次插入数据量

    // 功能区标准限值缓存
    private Map<String, NoiseStandard> standardCache = new ConcurrentHashMap<>();

    /**
     * 生成模拟数据
     * 按照方案A生成7天×24小时×30个监测点的完整数据
     */
    public void generateSimulatedData() {
        log.info("开始生成模拟数据...");

        // 初始化标准缓存
        initStandardCache();

        // 获取所有监测点
        List<MonitorStation> stations = monitorStationMapper.selectList(null);
        if (stations.isEmpty()) {
            log.warn("未找到监测点，无法生成模拟数据");
            return;
        }

        log.info("找到 {} 个监测点", stations.size());

        // 计算开始时间（7天前）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date startDate = calendar.getTime();
        log.info("生成数据时间范围: {} 到今天", startDate);

        // 批量生成数据
        List<MonitorData> batchData = new ArrayList<>(batchSize);
        int totalCount = 0;

        // 按天循环
        for (int day = 0; day < days; day++) {
            // 按小时循环
            for (int hour = 0; hour < hoursPerDay; hour++) {
                // 设置当前时间
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_YEAR, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                Date monitorTime = calendar.getTime();

                // 按监测点循环
                for (MonitorStation station : stations) {
                    // 生成噪声值
                    BigDecimal noiseLevel = generateNoiseLevel(station, monitorTime);

                    // 创建监测数据
                    MonitorData data = createMonitorData(station, noiseLevel, monitorTime);
                    batchData.add(data);
                    totalCount++;

                    // 批量提交
                    if (batchData.size() >= batchSize) {
                        saveBatchData(batchData);
                        batchData.clear();
                    }
                }
            }
        }

        // 提交剩余数据
        if (!batchData.isEmpty()) {
            saveBatchData(batchData);
        }

        log.info("模拟数据生成完成，共生成 {} 条数据", totalCount);
    }

    /**
     * 初始化标准缓存
     */
    private void initStandardCache() {
        List<NoiseStandard> standards = noiseStandardMapper.selectList(null);
        for (NoiseStandard standard : standards) {
            standardCache.put(standard.getFunctionalZone(), standard);
        }
        log.info("标准缓存初始化完成，共 {} 个功能区标准", standardCache.size());
    }

    /**
     * 生成噪声值
     * 基于功能区标准 + 合理波动 + 时间规律
     */
    public BigDecimal generateNoiseLevel(MonitorStation station, Date monitorTime) {
        String functionalZone = station.getFunctionalZone();
        NoiseStandard standard = standardCache.get(functionalZone);
        if (standard == null) {
            log.warn("未找到功能区 {} 的标准，使用默认值", functionalZone);
            return BigDecimal.valueOf(50.0);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monitorTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isDayTime = hour >= 6 && hour < 22;

        // 基础值 = 标准值 + (-2到+3dB的随机波动)
        BigDecimal baseLimit = isDayTime ? standard.getDayLimit() : standard.getNightLimit();
        double randomBase = baseLimit.doubleValue() + (Math.random() * 5 - 2);

        // 时间规律模拟
        double timeFactor = 0;
        if (isDayTime) {
            // 工作日早晚高峰(7-9, 17-19) +3-6dB
            boolean isWeekday = dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
            if (isWeekday) {
                if ((hour >= 7 && hour < 9) || (hour >= 17 && hour < 19)) {
                    timeFactor = 3 + Math.random() * 3;
                }
            } else {
                // 周末白天平稳
                timeFactor = 1 + Math.random() * 2;
            }
        } else {
            // 深夜(0:00-6:00)：基础值-5-8dB
            if (hour >= 0 && hour < 6) {
                timeFactor = -5 - Math.random() * 3;
            } else {
                // 夜间略高
                timeFactor = 1 + Math.random() * 2;
            }
        }

        // 功能区差异
        double zoneFactor = 0;
        switch (functionalZone) {
            case "CLASS_1": // 居住区
                zoneFactor = Math.random() * 2 - 1; // 波动较小
                break;
            case "CLASS_2": // 混合区
                zoneFactor = Math.random() * 3 - 1.5;
                break;
            case "CLASS_3": // 工业区
                zoneFactor = 2 + Math.random() * 3; // 持续较高
                if (!isDayTime) {
                    zoneFactor -= 3; // 夜间降低
                }
                break;
            case "CLASS_4A": // 交通区
            case "CLASS_4B":
                zoneFactor = 3 + Math.random() * 4; // 波动较大
                break;
        }

        // 计算最终值
        double finalValue = randomBase + timeFactor + zoneFactor;
        // 确保噪声值在合理范围内
        finalValue = Math.max(30, Math.min(90, finalValue));

        return BigDecimal.valueOf(finalValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 创建监测数据对象
     */
    private MonitorData createMonitorData(MonitorStation station, BigDecimal noiseLevel, Date monitorTime) {
        MonitorData data = new MonitorData();
        data.setStationId(station.getId());
        data.setNoiseLevel(noiseLevel);
        data.setMonitorTime(monitorTime);
        data.setDataSource("SIMULATED");

        // 注意：monitor_date和period_type是数据库生成列，由数据库自动计算
        // 不需要手动设置这两个字段

        return data;
    }

    /**
     * 批量保存数据
     */
    private void saveBatchData(List<MonitorData> dataList) {
        if (dataList.isEmpty()) {
            return;
        }

        try {
            // 批量保存数据
            for (MonitorData data : dataList) {
                monitorDataMapper.insert(data);
            }
            log.info("批量保存 {} 条数据成功", dataList.size());
        } catch (Exception e) {
            log.error("批量保存数据失败", e);
        }
    }

    /**
     * 清空现有模拟数据
     */
    public void clearSimulatedData() {
        try {
            monitorDataMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                    .eq("data_source", "SIMULATED"));
            log.info("清空模拟数据成功");
        } catch (Exception e) {
            log.error("清空模拟数据失败", e);
        }
    }

    /**
     * 设置生成天数
     */
    public void setDays(int days) {
        this.days = days;
    }

    /**
     * 设置每批次插入数据量
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 定时生成模拟数据
     * 每5分钟生成一次当前时间的监测数据
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void generateRealTimeSimulatedData() {
        log.info("开始生成实时模拟数据...");

        // 初始化标准缓存
        initStandardCache();

        // 获取所有监测点
        List<MonitorStation> stations = monitorStationMapper.selectList(null);
        if (stations.isEmpty()) {
            log.warn("未找到监测点，无法生成模拟数据");
            return;
        }

        log.info("找到 {} 个监测点，生成实时数据", stations.size());

        // 使用当前时间
        Date currentTime = new Date();
        log.info("生成数据时间: {}", currentTime);

        // 为每个监测点生成当前时间的数据
        List<MonitorData> batchData = new ArrayList<>(stations.size());

        for (MonitorStation station : stations) {
            // 生成噪声值
            BigDecimal noiseLevel = generateNoiseLevel(station, currentTime);

            // 创建监测数据
            MonitorData data = createMonitorData(station, noiseLevel, currentTime);
            batchData.add(data);
        }

        // 保存数据
        if (!batchData.isEmpty()) {
            saveBatchData(batchData);
        }

        log.info("实时模拟数据生成完成，共生成 {} 条数据", batchData.size());
    }
}