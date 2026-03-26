<template>
  <div class="dashboard-container">
    <h2 class="page-title">
      <el-icon><DataAnalysis /></el-icon>
      仪表盘
    </h2>
    
    <!-- 加载状态 -->
    <LoadingSpinner :visible="loading" text="加载中..." />
    
    <!-- 错误状态 -->
    <ErrorOverlay :visible="!!error" :message="error" @retry="retryLoad" />
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <StatCard 
            title="总投诉数" 
            :value="stats.total" 
            icon="DocumentChecked" 
            :iconColor="'var(--primary-color)'"
            trend="up"
            :trendValue="12"
          />
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <StatCard 
            title="已处理" 
            :value="stats.processed" 
            icon="Check" 
            :iconColor="'var(--success-color)'"
            trend="up"
            :trendValue="8"
          />
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <StatCard 
            title="待处理" 
            :value="stats.pending" 
            icon="Warning" 
            :iconColor="'var(--warning-color)'"
            trend="down"
            :trendValue="5"
          />
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <StatCard 
            title="活跃用户" 
            :value="stats.activeUsers" 
            icon="UserFilled" 
            :iconColor="'var(--info-color)'"
            trend="up"
            :trendValue="15"
          />
        </div>
      </el-col>
    </el-row>
    
    <!-- 最近投诉列表 -->
    <div class="recent-complaints-section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Ticket /></el-icon>
          最近投诉
        </h3>
        <el-button type="primary" size="small" @click="viewAllComplaints">
          <el-icon><ArrowRight /></el-icon>
          查看全部
        </el-button>
      </div>
      
      <div class="complaints-table">
        <el-table 
          :data="recentComplaints" 
          class="dashboard-table"
          :empty-text="'暂无投诉数据'"
        >
          <el-table-column label="编号" width="80" align="center">
            <template #default="scope">
              <span class="complaint-id">{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="scope">
              <span class="complaint-title">{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" min-width="150">
            <template #default="scope">
              <span class="complaint-location">{{ scope.row.location }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="scope">
              <StatusTag :status="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" align="center">
            <template #default="scope">
              <span class="complaint-time">{{ formatTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                link
                @click="viewDetail(scope.row.id)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Ticket, ArrowRight } from '@element-plus/icons-vue'
import StatCard from '@/components/business/StatCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorOverlay from '@/components/common/ErrorOverlay.vue'
import StatusTag from '@/components/business/StatusTag.vue'

const router = useRouter()

// 从后端API获取数据
const stats = ref({
  total: 0,
  processed: 0,
  pending: 0,
  activeUsers: 0
})

const recentComplaints = ref([])

const loading = ref(true)
const error = ref('')

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看全部投诉
const viewAllComplaints = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.role === 'ADMIN') {
    router.push('/app/admin/complaints')
  } else if (userInfo.role === 'WORKER') {
    router.push('/app/worker/tasks')
  } else {
    router.push('/app/complaint/list')
  }
}

// 从API获取概览数据
const fetchStats = async () => {
  try {
    // 获取用户角色
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    let apiUrl = ''
    
    if (userInfo.role === 'ADMIN') {
      // 管理员获取完整统计数据
      apiUrl = '/api/admin/statistics'
    } else if (userInfo.role === 'WORKER') {
      // 处理人员获取任务统计
      apiUrl = '/api/worker/statistics'
    } else {
      // 居民获取自己的投诉统计
      apiUrl = '/api/complaint/statistics'
    }
    
    const response = await fetch(apiUrl, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) {
      // 如果接口不存在，使用默认数据
      stats.value = {
        total: 0,
        processed: 0,
        pending: 0,
        activeUsers: 0
      }
      return
    }
    
    const data = await response.json()
    
    if (data.code === 200) {
      // 使用真实的统计数据
      stats.value = {
        total: data.data.totalComplaints || 0,
        processed: data.data.resolvedComplaints || 0,
        pending: data.data.pendingComplaints || 0,
        activeUsers: data.data.totalUsers || 0
      }
    } else {
      // 使用默认数据
      stats.value = {
        total: 0,
        processed: 0,
        pending: 0,
        activeUsers: 0
      }
    }
  } catch (err) {
    console.error('获取概览数据错误:', err)
    // 使用默认数据
    stats.value = {
      total: 0,
      processed: 0,
      pending: 0,
      activeUsers: 0
    }
  }
}

// 从API获取最近投诉
const fetchRecentComplaints = async () => {
  try {
    // 获取用户角色
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    let apiUrl = ''
    
    if (userInfo.role === 'ADMIN') {
      // 管理员获取所有投诉
      apiUrl = '/api/admin/complaints?page=1&size=5'
    } else if (userInfo.role === 'WORKER') {
      // 处理人员获取分配的任务
      apiUrl = '/api/worker/tasks?page=1&size=5'
    } else {
      // 居民获取自己的投诉
      apiUrl = '/api/complaint/my?page=1&size=5'
    }
    
    const response = await fetch(apiUrl, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) {
      throw new Error('获取最近投诉失败')
    }
    
    const data = await response.json()
    
    if (data.code === 200) {
      // 转换数据格式以匹配前端显示需求
      recentComplaints.value = (data.data.records || []).map(item => ({
        id: item.id,
        title: item.title,
        location: item.district || item.location || '',
        status: item.status,
        createTime: item.createTime
      }))
    } else {
      throw new Error(data.message || '获取最近投诉失败')
    }
  } catch (err) {
    console.error('获取最近投诉错误:', err)
    throw err
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 并行获取数据
    await Promise.all([
      fetchStats(),
      fetchRecentComplaints()
    ])
  } catch (err) {
    error.value = err.message || '加载数据失败'
    ElMessage.error(err.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化加载数据
onMounted(() => {
  loadData()
})

const retryLoad = () => {
  loadData()
}

const viewDetail = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}
</script>

<style scoped>
.dashboard-container {
  padding: var(--spacing-lg);
  min-height: calc(100vh - var(--header-height));
  background-color: var(--bg-secondary);
}

/* 统计卡片区域 */
.stats-row {
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  transition: all var(--transition-base);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

/* 最近投诉区域 */
.recent-complaints-section {
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--spacing-lg);
  transition: all var(--transition-base);
  border: 1px solid var(--border-secondary);
}

.recent-complaints-section:hover {
  box-shadow: var(--shadow-md);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-secondary);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0;
}

/* 表格样式 */
.complaints-table {
  overflow: hidden;
  border-radius: var(--border-radius-lg);
}

.dashboard-table {
  border-radius: var(--border-radius-lg) !important;
  overflow: hidden !important;
  border: 1px solid var(--border-secondary) !important;
}

.dashboard-table th {
  background-color: var(--bg-secondary) !important;
  font-weight: var(--font-weight-semibold) !important;
  color: var(--text-primary) !important;
  padding: var(--spacing-md) !important;
  border-bottom: 1px solid var(--border-secondary) !important;
}

.dashboard-table td {
  padding: var(--spacing-md) !important;
  border-bottom: 1px solid var(--border-tertiary) !important;
  transition: all var(--transition-fast) !important;
}

.dashboard-table tr:hover {
  background-color: var(--bg-light) !important;
}

.dashboard-table tr:last-child td {
  border-bottom: none !important;
}

/* 表格内容样式 */
.complaint-id {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--border-radius-full);
  background-color: var(--primary-light);
  color: var(--primary-color);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}

.complaint-title {
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.complaint-title:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

.complaint-location {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.complaint-time {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: var(--breakpoint-md)) {
  .dashboard-container {
    padding: var(--spacing-md);
  }
  
  .stats-row {
    margin-bottom: var(--spacing-lg);
  }
  
  .recent-complaints-section {
    padding: var(--spacing-md);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .section-title {
    font-size: var(--font-size-lg);
  }
  
  .dashboard-table th,
  .dashboard-table td {
    padding: var(--spacing-sm) !important;
    font-size: var(--font-size-sm) !important;
  }
  
  .dashboard-table .el-button {
    font-size: var(--font-size-xs) !important;
    padding: 0 !important;
  }
}

/* 加载状态和错误状态的覆盖样式 */
:deep(.loading-overlay) {
  border-radius: var(--border-radius-lg);
  background: rgba(255, 255, 255, 0.9);
}

:deep(.error-overlay) {
  border-radius: var(--border-radius-lg);
  background-color: var(--bg-primary);
  border: 1px solid var(--border-secondary);
  box-shadow: var(--shadow-md);
}
</style>