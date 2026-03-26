<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1>城市噪音监测与投诉系统</h1>
        <p>智能监测 · 快速响应 · 数据可视化</p>
        <div class="hero-buttons">
          <el-button type="primary" size="large" @click="handleLoginClick">{{ isLoggedIn() ? '进入系统' : '立即登录' }}</el-button>
          <el-button size="large" @click="scrollToFeatures">了解更多</el-button>
        </div>
      </div>
    </section>

    <!-- 核心功能 -->
    <section class="features-section" ref="featuresSection">
      <div class="container">
        <h2>核心功能</h2>
        <div class="features-grid">
          <el-card class="feature-card" @click="handleFeatureClick('/app/complaint/submit')">
            <template #header>
              <div class="feature-header">
                <el-icon class="feature-icon"><Bell /></el-icon>
                <span>噪音投诉管理</span>
              </div>
            </template>
            <div class="feature-content">
              <p>支持居民在线提交噪音投诉，处理人员快速响应，管理员实时监控处理进度，形成完整的投诉处理闭环。</p>
              <ul>
                <li>在线投诉提交</li>
                <li>智能任务分配</li>
                <li>处理进度跟踪</li>
                <li>结果反馈评价</li>
              </ul>
            </div>
          </el-card>

          <el-card class="feature-card" @click="handleFeatureClick('/app/complaint/map')">
            <template #header>
              <div class="feature-header">
                <el-icon class="feature-icon"><Location /></el-icon>
                <span>噪音地图可视化</span>
              </div>
            </template>
            <div class="feature-content">
              <p>将投诉点和监测点数据通过地图可视化展示，直观呈现城市噪音分布情况，支持多维度数据筛选。</p>
              <ul>
                <li>投诉点分布地图</li>
                <li>监测点实时数据</li>
                <li>多维度数据筛选</li>
              </ul>
            </div>
          </el-card>

          <el-card class="feature-card" @click="handleFeatureClick('/app/environment/noise-quality')">
            <template #header>
              <div class="feature-header">
                <el-icon class="feature-icon"><DataAnalysis /></el-icon>
                <span>声环境数据</span>
              </div>
            </template>
            <div class="feature-content">
              <p>提供城市声环境数据监测和分析，包括实时数据、历史趋势和区域对比，为噪音治理提供科学依据。</p>
              <ul>
                <li>实时噪音数据监测</li>
                <li>24小时趋势图表</li>
                <li>区域噪音对比</li>
                <li>环境噪声限值参考</li>
              </ul>
            </div>
          </el-card>

          <el-card class="feature-card" @click="handleFeatureClick('/app/notification/list')">
            <template #header>
              <div class="feature-header">
                <el-icon class="feature-icon"><Bell /></el-icon>
                <span>通知系统</span>
              </div>
            </template>
            <div class="feature-content">
              <p>实现投诉状态变更的实时通知，确保相关人员及时了解投诉处理进度，提高处理效率。</p>
              <ul>
                <li>状态变更通知</li>
                <li>未读消息标记</li>
                <li>通知历史记录</li>
                <li>批量删除通知</li>
              </ul>
            </div>
          </el-card>
        </div>
      </div>
    </section>

    <!-- 系统优势 -->
    <section class="advantages-section">
      <div class="container">
        <h2>系统优势</h2>
        <div class="advantages-grid">
          <div class="advantage-item">
            <div class="advantage-icon"><el-icon><Check /></el-icon></div>
            <h3>用户友好界面</h3>
            <p>采用现代化前端技术，提供直观、易用的用户界面，确保不同角色用户都能轻松操作。</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon"><el-icon><Check /></el-icon></div>
            <h3>多角色权限管理</h3>
            <p>基于RBAC模型实现精细化权限控制，为居民、处理人员和管理员分配不同操作权限。</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon"><el-icon><Check /></el-icon></div>
            <h3>实时通知系统</h3>
            <p>投诉状态变更自动发送通知，确保相关人员及时了解处理进度，提高工作效率。</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon"><el-icon><Check /></el-icon></div>
            <h3>数据可视化分析</h3>
            <p>通过地图和图表直观展示噪音数据，支持多维度分析，为决策提供科学依据。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据概览 -->
    <section class="stats-section">
      <div class="container">
        <h2>数据概览</h2>
        <div class="stats-grid">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ totalComplaints }}</div>
              <div class="stat-label">累计投诉</div>
            </div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ resolvedRate }}%</div>
              <div class="stat-label">解决率</div>
            </div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ monitorStations }}</div>
              <div class="stat-label">监测点数量</div>
            </div>
          </el-card>
        </div>
      </div>
    </section>

    <!-- 快速入口 -->
    <section class="quick-access-section">
      <div class="container">
        <h2>快速入口</h2>
        <div class="quick-access-grid">
          <el-card class="quick-card" @click="handleLoginClick">
            <div class="quick-content">
              <el-icon class="quick-icon"><User /></el-icon>
              <span>{{ isLoggedIn() ? '进入系统' : '用户登录' }}</span>
            </div>
          </el-card>
          <el-card class="quick-card" @click="handleFeatureClick('/app/complaint/submit')">
            <div class="quick-content">
              <el-icon class="quick-icon"><DocumentChecked /></el-icon>
              <span>投诉提交</span>
            </div>
          </el-card>
          <el-card class="quick-card" @click="handleFeatureClick('/app/complaint/map')">
            <div class="quick-content">
              <el-icon class="quick-icon"><Location /></el-icon>
              <span>噪音地图</span>
            </div>
          </el-card>
          <el-card class="quick-card" @click="handleFeatureClick('/app/environment/noise-quality')">
            <div class="quick-content">
              <el-icon class="quick-icon"><DataAnalysis /></el-icon>
              <span>声环境数据</span>
            </div>
          </el-card>
        </div>
      </div>
    </section>



    <!-- 页脚 -->
    <footer class="footer-section">
      <div class="container">
        <div class="footer-content">
          <div class="footer-info">
            <h3>城市噪音监测与投诉系统</h3>
            <p>© 2026 城市噪音监测与投诉系统 版权所有</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Monitor, Location, DataAnalysis, Check, User, DocumentChecked } from '@element-plus/icons-vue'

const router = useRouter()

// 引用
const featuresSection = ref(null)

// 检查登录状态
const isLoggedIn = () => {
  return !!localStorage.getItem('token')
}

// 处理功能跳转
const handleFeatureClick = (path) => {
  if (isLoggedIn()) {
    router.push(path)
  } else {
    router.push('/login')
  }
}

// 处理登录按钮点击
const handleLoginClick = () => {
  if (isLoggedIn()) {
    // 已登录，根据用户角色跳转到相应页面
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      if (userInfo && userInfo.role) {
        switch (userInfo.role) {
          case 'ADMIN':
            router.push('/app/dashboard')
            break
          case 'WORKER':
            router.push('/app/worker/tasks')
            break
          case 'RESIDENT':
            router.push('/app/complaint/submit')
            break
          default:
            router.push('/app/dashboard')
        }
      } else {
        router.push('/app/dashboard')
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
      router.push('/app/dashboard')
    }
  } else {
    // 未登录，跳转到登录页
    router.push('/login')
  }
}

// 数据概览
const totalComplaints = ref(0)
const resolvedRate = ref(0)
const monitorStations = ref(0)

// 加载真实数据
const loadRealData = async () => {
  try {
    // 1. 获取监测点数量
    const monitorResponse = await fetch('/api/common/monitor-stations')
    
    if (monitorResponse.ok) {
      const monitorData = await monitorResponse.json()
      if (monitorData.code === 200) {
        // 使用真实的监测点数量
        monitorStations.value = monitorData.data.length || 0
        console.log('监测点数量:', monitorStations.value)
      }
    } else {
      console.error('获取监测点失败:', monitorResponse.status)
      // API调用失败，使用默认数据
      monitorStations.value = 30
    }
    
    // 2. 获取投诉数据
    // 尝试从管理员统计API获取数据（可能需要权限）
    const statsResponse = await fetch('/api/admin/statistics', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      }
    })
    
    if (statsResponse.ok) {
      const statsData = await statsResponse.json()
      if (statsData.code === 200) {
        // 使用真实的投诉数据
        totalComplaints.value = statsData.data.totalComplaints || 0
        
        // 计算解决率
        const resolved = statsData.data.resolvedComplaints || 0
        const total = statsData.data.totalComplaints || 1
        resolvedRate.value = Math.round((resolved / total) * 100 * 10) / 10
        console.log('投诉数据:', totalComplaints.value, resolvedRate.value)
      }
    } else {
      console.error('获取统计数据失败:', statsResponse.status)
      // API调用失败，使用默认数据
      totalComplaints.value = 2
      resolvedRate.value = 50
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    // 网络错误，使用默认数据
    totalComplaints.value = 2
    resolvedRate.value = 50
    monitorStations.value = 30
  }
}



// 滚动到功能区域
const scrollToFeatures = () => {
  featuresSection.value.scrollIntoView({ behavior: 'smooth' })
}

// 初始化
onMounted(() => {
  // 加载真实数据
  loadRealData()
  console.log('首页加载完成')
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--bg-primary);
}

/* 顶部横幅 */
.hero-section {
  height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, #2575fc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  padding: 0 var(--spacing-lg);
}

.hero-content h1 {
  font-size: 3.5rem;
  margin-bottom: var(--spacing-lg);
  font-weight: bold;
}

.hero-content p {
  font-size: 1.5rem;
  margin-bottom: var(--spacing-xl);
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  margin-top: var(--spacing-xl);
}

/* 通用容器 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-lg);
}

/* 核心功能 */
.features-section {
  padding: var(--spacing-xxl) 0;
  background-color: var(--bg-secondary);
}

.features-section h2 {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  font-size: 2rem;
  color: var(--text-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.feature-card {
  transition: all var(--transition-base);
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.feature-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.feature-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
}

.feature-content {
  margin-top: var(--spacing-md);
}

.feature-content p {
  margin-bottom: var(--spacing-md);
  color: var(--text-secondary);
}

.feature-content ul {
  list-style: none;
  padding: 0;
}

.feature-content li {
  padding: var(--spacing-xs) 0;
  color: var(--text-tertiary);
  position: relative;
  padding-left: var(--spacing-lg);
}

.feature-content li::before {
  content: '•';
  color: var(--primary-color);
  position: absolute;
  left: 0;
}

/* 系统优势 */
.advantages-section {
  padding: var(--spacing-xxl) 0;
}

.advantages-section h2 {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  font-size: 2rem;
  color: var(--text-primary);
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
}

.advantage-item {
  text-align: center;
  padding: var(--spacing-lg);
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  transition: all var(--transition-base);
}

.advantage-item:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-sm);
}

.advantage-icon {
  font-size: 2rem;
  color: var(--success-color);
  margin-bottom: var(--spacing-md);
}

.advantage-item h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--text-primary);
}

.advantage-item p {
  color: var(--text-secondary);
}

/* 数据概览 */
.stats-section {
  padding: var(--spacing-xxl) 0;
  background-color: var(--bg-secondary);
}

.stats-section h2 {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  font-size: 2rem;
  color: var(--text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

/* 快速入口 */
.quick-access-section {
  padding: var(--spacing-xxl) 0;
}

.quick-access-section h2 {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  font-size: 2rem;
  color: var(--text-primary);
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-lg);
}

.quick-card {
  cursor: pointer;
  transition: all var(--transition-base);
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.quick-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg);
}

.quick-icon {
  font-size: 2rem;
  color: var(--primary-color);
  margin-bottom: var(--spacing-md);
}

/* 新闻公告 */
.news-section {
  padding: var(--spacing-xxl) 0;
  background-color: var(--bg-secondary);
}

.news-section h2 {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  font-size: 2rem;
  color: var(--text-primary);
}

/* 页脚 */
.footer-section {
  background-color: var(--bg-primary);
  border-top: 1px solid var(--border-primary);
  padding: var(--spacing-xl) 0;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-xl);
}

.footer-info h3 {
  margin-bottom: var(--spacing-md);
  color: var(--primary-color);
}

.footer-info p {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.footer-links h4,
.footer-contact h4 {
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
}

.footer-links ul {
  list-style: none;
  padding: 0;
}

.footer-links li {
  margin-bottom: var(--spacing-sm);
}

.footer-links a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--primary-color);
}

.footer-contact p {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 2.5rem;
  }
  
  .hero-content p {
    font-size: 1.2rem;
  }
  
  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .features-grid,
  .advantages-grid,
  .stats-grid,
  .quick-access-grid {
    grid-template-columns: 1fr;
  }
  
  .container {
    padding: var(--spacing-lg) var(--spacing-md);
  }
}
</style>