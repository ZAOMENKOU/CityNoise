import request from '@/utils/request'

// 监测数据相关API
export const monitorApi = {
  // 获取所有监测点
  getMonitorStations: () => request.get('/api/monitor/stations'),

  // 获取监测点地图点
  getMonitorMapPoints: () => request.get('/api/monitor/map-points'),

  // 获取超标监测点
  getOverStandardMonitors: () => request.get('/api/monitor/over-standard'),

  // 获取监测点最新数据
  getMonitorData: (stationId) => request.get('/api/monitor/data', { params: { stationId } }),

  // 获取监测点24小时数据
  getHourlyData: (stationId) => request.get('/api/monitor/hourly-data', { params: { stationId } }),

  // 获取环境噪声限值
  getNoiseStandards: () => request.get('/api/monitor/standards')
}
