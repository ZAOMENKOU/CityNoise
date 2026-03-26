import request from '@/utils/request'

// 管理员相关API
export const adminApi = {
  // 获取用户列表
  getUserList: (params) => request.get('/api/admin/users', { params }),

  // 添加用户
  addUser: (data) => request.post('/api/admin/users', data),

  // 更新用户
  updateUser: (id, data) => request.put(`/api/admin/users/${id}`, data),

  // 删除用户
  deleteUser: (id) => request.delete(`/api/admin/users/${id}`),

  // 获取投诉列表
  getComplaintList: (params) => request.get('/api/admin/complaints', { params }),

  // 分配任务
  assignTask: (id, data) => request.post(`/api/admin/complaint/${id}/assign`, data),

  // 关闭投诉
  closeComplaint: (id, data) => request.post(`/api/admin/complaint/${id}/close`, data),

  // 获取系统统计数据
  getStatistics: () => request.get('/api/admin/statistics'),

  // 任务监控
  getTaskMonitor: (params) => request.get('/api/admin/tasks/monitor', { params }),

  // 修改任务进度
  modifyTaskProgress: (id, data) => request.put(`/api/admin/task/${id}/progress`, data),

  // 查询任务修改历史
  getTaskHistory: (id) => request.get(`/api/admin/task/${id}/history`)
}
