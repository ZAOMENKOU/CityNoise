<template>
  <div class="notification-container">
    <h2>消息通知</h2>
    
    <div v-loading="loading" class="notification-bubbles">
      <el-card 
        v-for="(notification, index) in notifications" 
        :key="notification.id"
        class="notification-bubble"
        :class="{ 'unread': !notification.isRead }"
        shadow="hover"
      >
        <div class="bubble-header">
          <div class="bubble-title">
            <span class="notification-number">#{{ (currentPage - 1) * pageSize + index + 1 }}</span>
            <h3>{{ notification.title }}</h3>
          </div>
          <el-tag :type="notification.isRead ? 'info' : 'warning'">
            {{ notification.isRead ? '已读' : '未读' }}
          </el-tag>
        </div>
        <div class="bubble-body">
          <p class="notification-content">{{ notification.content }}</p>
        </div>
        <div class="bubble-footer">
          <span class="create-time">{{ notification.createTime }}</span>
          <div class="action-buttons">
            <el-button 
              v-if="!notification.isRead" 
              size="small" 
              @click="markAsRead(notification.id)"
            >
              标记已读
            </el-button>
            <el-button 
              v-if="notification.relatedId" 
              size="small" 
              type="primary" 
              @click="viewRelatedComplaint(notification.relatedId)"
            >
              查看投诉
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteNotification(notification.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 空状态 -->
      <div v-if="notifications.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无通知" />
      </div>
    </div>
    
    <div style="margin-top: 20px;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const notifications = ref([])

const fetchNotifications = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/notification/list?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      notifications.value = data.data.records
      total.value = data.data.total
    } else {
      ElMessage.error(data.message || '获取消息列表失败')
    }
  } catch (error) {
    console.error('获取消息列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id) => {
  try {
    const response = await fetch(`/api/notification/${id}/read`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('标记已读成功')
      // 更新本地状态
      const notification = notifications.value.find(item => item.id === id)
      if (notification) {
        notification.isRead = true
      }
      // 更新未读通知数
      await fetchUnreadCount()
    } else {
      ElMessage.error(data.message || '标记已读失败')
    }
  } catch (error) {
    console.error('标记已读错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

const viewRelatedComplaint = (complaintId) => {
  router.push(`/app/complaint/detail/${complaintId}`)
}

const deleteNotification = async (id) => {
  try {
    const response = await fetch(`/api/notification/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('删除消息成功')
      // 重新获取消息列表，序号会自动重新计算
      fetchNotifications()
      // 更新未读通知数
      await fetchUnreadCount()
    } else {
      ElMessage.error(data.message || '删除消息失败')
    }
  } catch (error) {
    console.error('删除消息错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 获取未读通知数
const fetchUnreadCount = async () => {
  try {
    // 调用全局方法更新未读通知数
    if (window.updateUnreadCount) {
      await window.updateUnreadCount()
    }
  } catch (error) {
    console.error('获取未读通知数错误:', error)
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchNotifications()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notification-container {
  padding: 16px;
  max-width: 100%;
}

.notification-bubbles {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-bubble {
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background-color: #ffffff;
  margin-bottom: 0;
}

.notification-bubble:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.notification-bubble.unread {
  border-left: 4px solid #409EFF;
}

.bubble-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.bubble-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-number {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.bubble-title h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
  flex: 1;
  word-break: break-word;
}

.bubble-body {
  margin-bottom: 12px;
}

.notification-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
  margin: 0;
  word-break: break-word;
}

.bubble-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.create-time {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  padding: 4px 12px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notification-container {
    padding: 12px;
  }
  
  .notification-bubble {
    padding: 12px;
  }
  
  .bubble-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .bubble-title {
    flex-wrap: wrap;
  }
  
  .bubble-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: flex-start;
  }
  
  .action-buttons .el-button {
    flex: 1;
    min-width: 80px;
  }
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>