import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 初始化，保持与现有代码兼容
  const storedUser = localStorage.getItem('userInfo')
  const userInfo = ref(storedUser ? JSON.parse(storedUser) : null)
  
  // 计算属性：判断用户角色
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isWorker = computed(() => userInfo.value?.role === 'WORKER')
  const isResident = computed(() => userInfo.value?.role === 'RESIDENT')
  
  // 获取用户名
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')
  
  // Actions：更新用户信息
  const updateUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  // Actions：登出
  const logout = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }
  
  // Actions：从 localStorage 重新加载
  const reloadFromStorage = () => {
    const stored = localStorage.getItem('userInfo')
    userInfo.value = stored ? JSON.parse(stored) : null
  }
  
  return {
    userInfo,
    isAdmin,
    isWorker,
    isResident,
    username,
    realName,
    updateUserInfo,
    logout,
    reloadFromStorage
  }
})
