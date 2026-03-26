<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-left">
        <!-- 汉堡按钮，在所有设备上都显示 -->
        <el-button
          v-if="isLoggedIn"
          circle
          class="menu-toggle"
          @click="toggleSidebar"
          :icon="Menu"
        />
        <div class="logo">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <span>城市噪音监测与投诉系统</span>
        </div>
      </div>
      <div class="nav-right">
        <template v-if="isLoggedIn">
          <!-- 通知图标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-button
              circle
              class="notification-btn"
              @click="router.push('/app/notification/list')"
              :icon="Bell"
            />
          </el-badge>
          
          <el-dropdown class="user-dropdown">
            <div class="user-info">
              <div class="user-avatar">{{ userInitial }}</div>
              <span class="user-name">{{ userInfo.realName || '用户' }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="dropdown-menu">
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 侧边栏 -->
      <aside class="sidebar" v-if="isLoggedIn" :class="{ 'sidebar-hidden': !sidebarVisible }">
        <div class="sidebar-header">
          <el-icon class="sidebar-logo"><Monitor /></el-icon>
          <span v-if="sidebarVisible" class="sidebar-title">系统菜单</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          router
        >
          <!-- 动态生成菜单 -->
          <template v-for="menuItem in sidebarMenu" :key="menuItem.index">
            <el-menu-item 
              v-if="!menuItem.children" 
              :index="menuItem.index"
              class="menu-item"
            >
              <el-icon class="menu-icon">
                <component :is="menuItem.icon === 'DocumentChecked' ? DocumentChecked : 
                  menuItem.icon === 'Ticket' ? Ticket : 
                  menuItem.icon === 'Monitor' ? Monitor : 
                  menuItem.icon === 'DataAnalysis' ? DataAnalysis : 
                  menuItem.icon === 'User' ? User : 
                  menuItem.icon === 'History' ? Timer : 
                  menuItem.icon === 'Users' ? UserFilled : 
                  menuItem.icon === 'TrendingUp' ? Histogram : 
                  DocumentChecked" />
              </el-icon>
              <span v-if="sidebarVisible" class="menu-text">{{ menuItem.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </aside>

      <!-- 页面内容区域 -->
      <main class="content-area" :class="{ 'content-area-expanded': !sidebarVisible && !isMobile }">
        <template v-if="isLoggedIn">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </template>
        <template v-else>
          <div class="not-logged-in">
            <el-icon class="login-icon"><Lock /></el-icon>
            <h2>请先登录</h2>
            <p>您需要登录才能访问系统功能</p>
            <el-button type="primary" @click="router.push('/login')">前往登录</el-button>
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, ArrowDown, DocumentChecked, User, DataAnalysis, Ticket, Timer, UserFilled, Monitor, Histogram, Menu, Lock, SwitchButton } from '@element-plus/icons-vue'

// 从localStorage中读取用户信息
const userInfo = ref({
  realName: '',
  role: ''
})

const router = useRouter()
const route = useRoute()

// 未读通知数
const unreadCount = ref(0)

// 侧边栏控制
const sidebarVisible = ref(true)
const isMobile = ref(window.innerWidth <= 768)

// 计算用户姓名首字母
const userInitial = computed(() => {
  if (!userInfo.value.realName) return 'U'
  return userInfo.value.realName.charAt(0).toUpperCase()
})

// 切换侧边栏显示/隐藏
const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

// 监听窗口大小变化
const handleResize = () => {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth <= 768
  
  // 在移动端默认收起侧边栏
  if (isMobile.value && !wasMobile) {
    sidebarVisible.value = false
  }
  // 在桌面端默认显示侧边栏
  if (!isMobile.value && wasMobile) {
    sidebarVisible.value = true
  }
}

// 检查用户是否已登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token') && !!userInfo.value.realName
})

// 当前激活的菜单项（使用完整的路径）
const activeMenu = computed(() => {
  return route.path
})

// 根据用户角色生成侧边栏菜单
const sidebarMenu = computed(() => {
  const role = userInfo.value.role
  
  switch (role) {
    case 'RESIDENT':
      return [
        {
          index: '/app/complaint/submit',
          title: '提交投诉',
          icon: 'DocumentChecked'
        },
        {
          index: '/app/complaint/list',
          title: '投诉列表',
          icon: 'Ticket'
        },
        {
          index: '/app/complaint/map',
          title: '噪音地图',
          icon: 'Monitor'
        },
        {
          index: '/app/environment/noise-quality',
          title: '声环境数据',
          icon: 'DataAnalysis'
        }
      ]
    case 'WORKER':
      return [
        {
          index: '/app/worker/tasks',
          title: '我的任务',
          icon: 'User'
        },
        {
          index: '/app/worker/history',
          title: '处理历史',
          icon: 'History'
        },
        {
          index: '/app/complaint/map',
          title: '噪音地图',
          icon: 'Monitor'
        },
        {
          index: '/app/environment/noise-quality',
          title: '声环境数据',
          icon: 'DataAnalysis'
        }
      ]
    case 'ADMIN':
      return [
        {
          index: '/app/admin/users',
          title: '用户管理',
          icon: 'Users'
        },
        {
          index: '/app/admin/complaints',
          title: '投诉管理',
          icon: 'Ticket'
        },
        {
          index: '/app/admin/task-monitor',
          title: '任务监控',
          icon: 'Monitor'
        },
        {
          index: '/app/complaint/map',
          title: '噪音地图',
          icon: 'Monitor'
        },
        {
          index: '/app/environment/noise-quality',
          title: '声环境数据',
          icon: 'DataAnalysis'
        },
        {
          index: '/app/admin/statistics',
          title: '统计分析',
          icon: 'TrendingUp'
        }
      ]
    default:
      return []
  }
})

// 菜单选择处理
const handleMenuSelect = (index) => {
  router.push(index)
}

// 退出登录
const handleLogout = () => {
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 重置用户信息
  userInfo.value = {
    realName: '',
    role: ''
  }
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 加载用户信息
const loadUserInfo = async () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
      // 从localStorage读取用户信息后，获取未读通知数
      await fetchUnreadCount()
    } catch (error) {
      console.error('解析用户信息失败:', error)
      // 解析失败，清除本地存储
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      router.push('/login')
    }
  } else {
    // 没有用户信息，检查是否有token
    if (localStorage.getItem('token')) {
      // 有token但没有用户信息，尝试从API获取
      await fetchUserInfoFromApi()
    }
  }
}

// 从API获取用户信息
const fetchUserInfoFromApi = async () => {
  try {
    const response = await fetch('/api/auth/me', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      userInfo.value = data.data
      localStorage.setItem('userInfo', JSON.stringify(data.data))
      
      // 获取未读通知数
      await fetchUnreadCount()
    } else {
      // 获取用户信息失败，清除token
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    // 网络错误，清除token
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}

// 获取未读通知数
const fetchUnreadCount = async () => {
  try {
    const response = await fetch('/api/notification/unread-count', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      unreadCount.value = data.data || 0
    }
  } catch (error) {
    console.error('获取未读通知数错误:', error)
  }
}

// 初始化
onMounted(async () => {
  // 检查是否需要重定向到登录页
  if (route.path !== '/login' && !localStorage.getItem('token')) {
    router.push('/login')
  } else if (localStorage.getItem('token')) {
    // 已登录，加载用户信息
    await loadUserInfo()
    // 确保获取未读通知数
    await fetchUnreadCount()
  }
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 移除全局方法
  delete window.updateUnreadCount
})

// 添加全局方法，用于其他组件更新未读通知数
window.updateUnreadCount = async () => {
  if (localStorage.getItem('token')) {
    await fetchUnreadCount()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
}

/* 顶部导航栏 */
.top-nav {
  height: var(--header-height);
  background-color: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--border-secondary);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  position: sticky;
  top: 0;
  z-index: var(--z-index-sticky);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
  transition: all var(--transition-fast);
}

.logo:hover {
  color: var(--primary-hover);
}

.logo-icon {
  font-size: var(--font-size-xl);
  color: var(--primary-color);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.menu-toggle {
  display: block;
  transition: all var(--transition-fast);
}

.menu-toggle:hover {
  background-color: var(--bg-secondary);
}

/* 通知按钮 */
.notification-badge {
  margin-right: var(--spacing-sm);
}

.notification-btn {
  transition: all var(--transition-fast);
}

.notification-btn:hover {
  background-color: var(--bg-secondary);
}

/* 用户下拉菜单 */
.user-dropdown {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-full);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.user-info:hover {
  background-color: var(--bg-secondary);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--border-radius-full);
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
}

.user-info:hover .user-avatar {
  background-color: var(--primary-hover);
  transform: scale(1.05);
}

.user-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  white-space: nowrap;
}

.dropdown-icon {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.user-info:hover .dropdown-icon {
  color: var(--primary-color);
  transform: rotate(180deg);
}

.dropdown-menu {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-secondary);
  overflow: hidden;
}

.dropdown-menu .el-dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  transition: all var(--transition-fast);
}

.dropdown-menu .el-dropdown-item:hover {
  background-color: var(--bg-secondary);
  color: var(--primary-color);
}

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width);
  height: calc(100vh - var(--header-height));
  overflow-y: auto;
  border-right: 1px solid var(--border-secondary);
  transition: all var(--transition-base);
  background-color: var(--bg-primary);
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: relative;
}

.sidebar-hidden {
  width: var(--sidebar-width-collapsed);
  overflow: hidden;
  border-right: 1px solid var(--border-secondary);
}

/* 侧边栏头部 */
.sidebar-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-secondary);
  transition: all var(--transition-base);
}

.sidebar-logo {
  font-size: var(--font-size-xl);
  color: var(--primary-color);
  transition: all var(--transition-fast);
}

.sidebar-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  transition: all var(--transition-base);
}

/* 侧边栏菜单 */
.sidebar-menu {
  border-right: none !important;
  background-color: transparent !important;
  padding: var(--spacing-md) 0;
}

.menu-item {
  height: 56px !important;
  line-height: 56px !important;
  margin: var(--spacing-xs) var(--spacing-md) !important;
  border-radius: var(--border-radius-lg) !important;
  transition: all var(--transition-fast) !important;
  display: flex !important;
  align-items: center !important;
  gap: var(--spacing-sm) !important;
  padding: 0 var(--spacing-md) !important;
}

.menu-item:hover {
  background-color: var(--bg-secondary) !important;
  color: var(--primary-color) !important;
  transform: translateX(4px) !important;
}

.menu-item.is-active {
  background-color: var(--primary-color) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(51, 102, 255, 0.3) !important;
  transform: translateX(4px) !important;
}

.menu-item.is-active .menu-icon {
  color: white !important;
}

.menu-icon {
  font-size: var(--font-size-lg) !important;
  transition: all var(--transition-fast) !important;
}

.menu-item:hover .menu-icon {
  color: var(--primary-color) !important;
  transform: scale(1.1) !important;
}

.menu-text {
  font-size: var(--font-size-base) !important;
  font-weight: var(--font-weight-medium) !important;
  transition: all var(--transition-fast) !important;
}

/* 主体内容 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.content-area {
  flex: 1;
  padding: var(--spacing-lg);
  overflow-y: auto;
  background-color: var(--bg-secondary);
  transition: all var(--transition-base);
  position: relative;
}

.content-area-expanded {
  flex: 1;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: var(--header-height);
    z-index: var(--z-index-fixed);
    width: 260px;
    box-shadow: 2px 0 16px rgba(0, 0, 0, 0.15);
  }
  
  .sidebar-hidden {
    transform: translateX(-100%);
    width: 260px;
  }
  
  .content-area {
    padding: var(--spacing-md);
  }
  
  .top-nav {
    padding: 0 var(--spacing-md);
  }
  
  .logo {
    font-size: var(--font-size-base);
  }
  
  .nav-right {
    gap: var(--spacing-sm);
  }
  
  .notification-badge {
    margin-right: 0;
  }
  
  .user-name {
    display: none;
  }
  
  .menu-item {
    padding: 0 var(--spacing-lg) !important;
  }
}

/* 未登录状态样式 */
.not-logged-in {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
  padding: var(--spacing-4xl);
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  margin: var(--spacing-md);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-secondary);
}

.login-icon {
  font-size: 4rem;
  color: var(--primary-color);
  margin-bottom: var(--spacing-lg);
  opacity: 0.8;
  transition: all var(--transition-base);
}

.not-logged-in:hover .login-icon {
  opacity: 1;
  transform: scale(1.1);
}

.not-logged-in h2 {
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
}

.not-logged-in p {
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-xl);
  font-size: var(--font-size-base);
  max-width: 400px;
  line-height: var(--line-height-lg);
}

.not-logged-in .el-button {
  font-size: var(--font-size-base);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  transition: all var(--transition-fast);
}

.not-logged-in .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(51, 102, 255, 0.3);
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: var(--border-radius-full);
}

.sidebar::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: var(--border-radius-full);
  transition: all var(--transition-fast);
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: var(--text-quaternary);
}

.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: var(--border-radius-full);
}

.content-area::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: var(--border-radius-full);
  transition: all var(--transition-fast);
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: var(--text-quaternary);
}
</style>