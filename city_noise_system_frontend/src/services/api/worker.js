import request from '@/utils/request'

// 处理人员相关API
export const workerApi = {
  // 获取我的任务列表
  getMyTasks: (params) => request.get('/api/worker/tasks', { params }),

  // 开始处理任务
  startTask: (id) => request.post(`/api/worker/task/${id}/start`),

  // 更新任务进度
  updateTaskProgress: (id, data) => request.put(`/api/worker/task/${id}/progress`, data),

  // 完成任务
  resolveTask: (id, data) => request.post(`/api/worker/task/${id}/resolve`, data),

  // 获取任务处理记录
  getTaskRecords: (id) => request.get(`/api/worker/task/${id}/records`),

  // 获取处理历史
  getProcessingHistory: (params) => request.get('/api/worker/history', { params })
}
