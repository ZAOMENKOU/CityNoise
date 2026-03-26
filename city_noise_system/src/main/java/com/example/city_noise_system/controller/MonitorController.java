package com.example.city_noise_system.controller;

import com.example.city_noise_system.entity.MonitorStation;
import com.example.city_noise_system.entity.MonitorData;
import com.example.city_noise_system.entity.NoiseStandard;
import com.example.city_noise_system.mapper.MonitorStationMapper;
import com.example.city_noise_system.mapper.MonitorDataMapper;
import com.example.city_noise_system.mapper.NoiseStandardMapper;
import com.example.city_noise_system.utils.SimulatedDataGenerator;
import com.example.city_noise_system.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监测点控制器
 * 处理监测点相关的接口，包括获取监测点列表、监测数据、超标预警等
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private MonitorStationMapper monitorStationMapper;

    @Autowired
    private MonitorDataMapper monitorDataMapper;

    @Autowired
    private NoiseStandardMapper noiseStandardMapper;

    @Autowired
    private SimulatedDataGenerator simulatedDataGenerator;

    /**
     * 获取所有监测点
     * GET /api/monitor/stations
     * 需要JWT Token认证
     */
    @GetMapping("/stations")
    public ResultVO<?> getMonitorStations() {
        try {
            List<MonitorStation> stations = monitorStationMapper.selectList(null);
            return ResultVO.success(stations);
        } catch (Exception e) {
            return ResultVO.error(500, "获取监测点失败: " + e.getMessage());
        }
    }

    /**
     * 获取监测点地图点
     * GET /api/monitor/map-points
     * 需要JWT Token认证
     */
    @GetMapping("/map-points")
    public ResultVO<?> getMonitorMapPoints() {
        try {
            // 获取所有监测点
            List<MonitorStation> stations = monitorStationMapper.selectList(null);

            // 构建响应数据
            List<Map<String, Object>> mapPoints = new ArrayList<>();

            for (MonitorStation station : stations) {
                // 获取最新的监测数据
                List<MonitorData> latestData = monitorDataMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                                .eq("station_id", station.getId())
                                .orderByDesc("monitor_time")
                                .last("LIMIT 1"));

                Map<String, Object> point = new HashMap<>();
                point.put("id", station.getId());
                point.put("stationName", station.getStationName());
                point.put("longitude", station.getLongitude());
                point.put("latitude", station.getLatitude());
                point.put("district", station.getDistrict());
                point.put("functionalZone", station.getFunctionalZone());
                point.put("address", station.getAddress());

                if (!latestData.isEmpty()) {
                    MonitorData data = latestData.get(0);
                    point.put("noiseLevel", data.getNoiseLevel());
                    point.put("monitorTime", data.getMonitorTime());
                    point.put("periodType", data.getPeriodType());
                }

                mapPoints.add(point);
            }

            return ResultVO.success(mapPoints);
        } catch (Exception e) {
            return ResultVO.error(500, "获取监测点地图数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取声标准数据
     * GET /api/monitor/standards
     * 需要JWT Token认证
     */
    @GetMapping("/standards")
    public ResultVO<?> getNoiseStandards() {
        try {
            List<NoiseStandard> standards = noiseStandardMapper.selectList(null);
            return ResultVO.success(standards);
        } catch (Exception e) {
            return ResultVO.error(500, "获取声标准数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取超标监测点
     * GET /api/monitor/over-standard
     * 需要JWT Token认证
     */
    @GetMapping("/over-standard")
    public ResultVO<?> getOverStandardMonitors() {
        try {
            // 获取所有声标准
            List<NoiseStandard> standards = noiseStandardMapper.selectList(null);
            Map<String, NoiseStandard> standardMap = new HashMap<>();
            for (NoiseStandard standard : standards) {
                standardMap.put(standard.getFunctionalZone(), standard);
            }

            // 获取所有监测点
            List<MonitorStation> stations = monitorStationMapper.selectList(null);
            List<Map<String, Object>> overStandardList = new ArrayList<>();

            for (MonitorStation station : stations) {
                // 获取最新的监测数据
                List<MonitorData> latestData = monitorDataMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                                .eq("station_id", station.getId())
                                .orderByDesc("monitor_time")
                                .last("LIMIT 1"));

                if (!latestData.isEmpty()) {
                    MonitorData data = latestData.get(0);
                    NoiseStandard standard = standardMap.get(station.getFunctionalZone());

                    if (standard != null) {
                        // 判断是否超标
                        boolean isOverStandard = false;
                        Double limitValue = null;

                        if ("DAY".equals(data.getPeriodType())) {
                            isOverStandard = data.getNoiseLevel().compareTo(standard.getDayLimit()) > 0;
                            limitValue = standard.getDayLimit().doubleValue();
                        } else if ("NIGHT".equals(data.getPeriodType())) {
                            isOverStandard = data.getNoiseLevel().compareTo(standard.getNightLimit()) > 0;
                            limitValue = standard.getNightLimit().doubleValue();
                        }

                        if (isOverStandard) {
                            Map<String, Object> overStandardData = new HashMap<>();
                            overStandardData.put("stationId", station.getId());
                            overStandardData.put("stationName", station.getStationName());
                            overStandardData.put("address", station.getAddress());
                            overStandardData.put("noiseLevel", data.getNoiseLevel());
                            overStandardData.put("limitValue", limitValue);
                            overStandardData.put("periodType", data.getPeriodType());
                            overStandardData.put("monitorTime", data.getMonitorTime());
                            overStandardData.put("functionalZone", station.getFunctionalZone());

                            overStandardList.add(overStandardData);
                        }
                    }
                }
            }

            return ResultVO.success(overStandardList);
        } catch (Exception e) {
            return ResultVO.error(500, "获取超标监测点失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取监测点最新数据
     * GET /api/monitor/data?stationId={stationId}
     * 需要JWT Token认证
     */
    @GetMapping("/data")
    public ResultVO<?> getMonitorData(Long stationId) {
        try {
            if (stationId == null) {
                return ResultVO.error(400, "stationId不能为空");
            }
            
            // 获取最新的监测数据
            List<MonitorData> latestData = monitorDataMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                            .eq("station_id", stationId)
                            .orderByDesc("monitor_time")
                            .last("LIMIT 10") // 获取最新的10条数据
            );
            
            return ResultVO.success(latestData);
        } catch (Exception e) {
            return ResultVO.error(500, "获取监测点数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取监测点24小时数据
     * GET /api/monitor/hourly-data?stationId={stationId}
     * 需要JWT Token认证
     */
    @GetMapping("/hourly-data")
    public ResultVO<?> getHourlyData(Long stationId) {
        try {
            if (stationId == null) {
                return ResultVO.error(400, "stationId不能为空");
            }
            
            // 获取24小时数据
            List<MonitorData> hourlyData = monitorDataMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorData>()
                            .eq("station_id", stationId)
                            .ge("monitor_time", "2026-02-22 16:00:00") // 模拟24小时前的数据
                            .le("monitor_time", "2026-02-23 16:00:00") // 模拟当前时间的数据
                            .orderByAsc("monitor_time")
            );
            
            // 构建响应数据
            List<String> hours = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            
            // 如果没有数据，生成模拟数据
            if (hourlyData.isEmpty()) {
                // 生成24小时的模拟数据
                for (int i = 0; i < 24; i++) {
                    int hour = i + 16;
                    if (hour > 23) {
                        hour -= 24;
                    }
                    hours.add(String.format("%02d:00", hour));
                    
                    // 生成合理的模拟数据
                    double baseValue = 50;
                    if (hour >= 0 && hour < 6) {
                        // 凌晨
                        baseValue = 45 + Math.random() * 5;
                    } else if (hour >= 6 && hour < 12) {
                        // 上午
                        baseValue = 52 + Math.random() * 5;
                    } else if (hour >= 12 && hour < 18) {
                        // 下午
                        baseValue = 55 + Math.random() * 5;
                    } else if (hour >= 18 && hour < 22) {
                        // 晚上
                        baseValue = 53 + Math.random() * 5;
                    } else {
                        // 深夜
                        baseValue = 48 + Math.random() * 5;
                    }
                    values.add(Math.round(baseValue * 10) / 10.0);
                }
            } else {
                // 使用真实数据
                for (MonitorData data : hourlyData) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:00");
                    hours.add(sdf.format(data.getMonitorTime()));
                    values.add(data.getNoiseLevel().doubleValue());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("hours", hours);
            result.put("values", values);
            
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error(500, "获取24小时数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成模拟数据
     * GET /api/monitor/generate-data
     * 用于手动触发模拟数据生成
     */
    @GetMapping("/generate-data")
    public ResultVO<?> generateSimulatedData() {
        try {
            simulatedDataGenerator.generateSimulatedData();
            return ResultVO.success("模拟数据生成成功");
        } catch (Exception e) {
            return ResultVO.error(500, "生成模拟数据失败: " + e.getMessage());
        }
    }
}
