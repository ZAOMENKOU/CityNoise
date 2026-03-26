import request from '@/utils/request'

// 公共模块相关API
export const commonApi = {
  // 获取通知列表
  getNotifications: (params) => request.get('/api/common/notifications', { params }),

  // 获取未读通知数量
  getUnreadCount: () => request.get('/api/common/notifications/unread-count'),

  // 标记通知为已读
  markAsRead: (id) => request.put(`/api/common/notifications/${id}/read`),

  // 标记所有通知为已读
  markAllAsRead: () => request.put('/api/common/notifications/read-all'),

  // 删除通知
  deleteNotification: (id) => request.delete(`/api/common/notifications/${id}`),

  // 获取监测点列表
  getMonitorStations: (params) => request.get('/api/common/monitor-stations', { params }),

  // 生成模拟监测数据
  generateSimulatedData: () => request.post('/api/common/monitor-data/simulate'),

  // 获取首页概览数据
  getDashboardOverview: () => request.get('/api/common/dashboard/overview'),

  // 获取所有行政区列表
  getDistricts: () => request.get('/api/common/districts')
}
