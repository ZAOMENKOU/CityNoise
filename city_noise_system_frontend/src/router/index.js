import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'

const routes = [

  // 根路径重定向到首页
  {
    path: '/',
    redirect: '/home',
    meta: { requiresAuth: false }
  },

  // 首页
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/HomeView.vue'),
    meta: { title: '首页', requiresAuth: false }
  },

  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },

  // 注册页
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', requiresAuth: false }
  },

  // 主要布局路由
  {
    path: '/app',
    component: Layout,
    children: [
      {
        path: 'complaint',
        redirect: 'complaint/submit',
        meta: { requiresAuth: true }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'complaint/list',
        name: 'ComplaintList',
        component: () => import('@/views/complaint/ComplaintListView.vue'),
        meta: { title: '投诉列表', requiresAuth: true }
      },
      {
        path: 'complaint/submit',
        name: 'ComplaintSubmit',
        component: () => import('@/views/complaint/ComplaintSubmitView.vue'),
        meta: { title: '提交投诉', requiresAuth: true }
      },
      {
        path: 'complaint/detail/:id',
        name: 'ComplaintDetail',
        component: () => import('@/views/complaint/Detail.vue'),
        meta: { title: '投诉详情', requiresAuth: true }
      },
      {
        path: 'complaint/map',
        name: 'NoiseMap',
        component: () => import('@/views/complaint/NoiseMapView.vue'),
        meta: { title: '噪音地图', requiresAuth: true }
      },
      {
        path: 'worker/tasks',
        name: 'WorkerTasks',
        component: () => import('@/views/worker/WorkerTasksView.vue'),
        meta: { title: '工作任务', requiresAuth: true }
      },
      {
        path: 'worker/history',
        name: 'WorkerHistory',
        component: () => import('@/views/worker/WorkerHistoryView.vue'),
        meta: { title: '处理历史', requiresAuth: true }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      {
        path: 'admin/complaints',
        name: 'AdminComplaints',
        component: () => import('@/views/admin/AdminComplaintsView.vue'),
        meta: { title: '投诉管理', requiresAuth: true }
      },
      {
        path: 'admin/statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '统计分析', requiresAuth: true }
      },
      {
        path: 'admin/task-monitor',
        name: 'TaskMonitor',
        component: () => import('@/views/admin/TaskMonitorView.vue'),
        meta: { title: '任务监控', requiresAuth: true }
      },
      {
        path: 'notification/list',
        name: 'NotificationList',
        component: () => import('@/views/notification/NotificationListView.vue'),
        meta: { title: '消息通知', requiresAuth: true }
      },
      {
        path: 'environment/noise-quality',
        name: 'NoiseQuality',
        component: () => import('@/views/environment/NoiseQualityView.vue'),
        meta: { title: '声环境质量数据', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 如果是开发模式路由，直接放行
  if (to.path.startsWith('/dev/')) {
    next()
    return
  }

  // 如果访问根路径，直接重定向到登录页
  if (to.path === '/') {
    next('/login')
    return
  }

  // 如果访问需要认证的页面但没有token，跳转到登录页
  if (to.matched.some(record => record.meta.requiresAuth) && !token) {
    next('/login')
    return
  }

  // 如果已登录且访问登录页，跳转到首页
  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  next()
})

export default router