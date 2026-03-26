import request from '@/utils/request'

// 投诉相关API
export const complaintApi = {
  // 获取我的投诉列表
  getMyComplaints: (params) => request.get('/api/complaint/my', { params }),

  // 提交投诉
  submitComplaint: (data) => request.post('/api/complaint/submit', data),

  // 获取投诉详情
  getComplaintDetail: (id) => request.get(`/api/complaint/${id}`),

  // 获取投诉处理记录
  getComplaintRecords: (id) => request.get(`/api/complaint/${id}/records`),

  // 补充投诉图片
  addComplaintImage: (id, data) => request.post(`/api/complaint/${id}/image`, data),

  // 标记投诉为未解决
  markAsUnsolved: (id, data) => request.post(`/api/complaint/${id}/unsolved`, data),

  // 获取投诉地图数据
  getComplaintMapData: (params) => request.get('/api/complaint/map-data', { params }),

  getDistrictStats: () => request.get('/api/admin/complaints/district-stats'),

  getNoiseTypeStats: () => request.get('/api/admin/complaints/noise-type-stats')
}
