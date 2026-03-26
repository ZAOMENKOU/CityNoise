import request from '@/utils/request'

// 认证相关API
export const authApi = {
  // 登录
  login: (data) => request.post('/api/auth/login', data),

  // 注册
  register: (data) => request.post('/api/auth/register', data),

  // 获取当前用户信息
  getCurrentUser: () => request.get('/api/auth/me'),

  // 登出
  logout: () => request.post('/api/auth/logout')
}
