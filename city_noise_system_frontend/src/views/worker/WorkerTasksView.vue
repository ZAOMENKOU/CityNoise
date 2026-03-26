<template>
  <div class="tasks-container">
    <h2>我的任务</h2>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待处理" name="pending">
        <div v-loading="loading" class="tasks-grid">
          <el-card 
            v-for="(task, index) in tasksList.pending" 
            :key="task.id"
            class="task-card"
            shadow="hover"
          >
            <div class="task-header">
              <div class="task-number">#{{ index + 1 }}</div>
              <el-tag type="info">待处理</el-tag>
            </div>
            <div class="task-title">{{ task.title }}</div>
            <div class="task-info">
              <div class="info-item">
                <span class="info-label">行政区：</span>
                <span class="info-value">{{ task.district }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间：</span>
                <span class="info-value">{{ task.createTime }}</span>
              </div>
            </div>
            <div class="task-actions">
              <el-button size="small" type="primary" @click="startTask(task.id)">开始处理</el-button>
              <el-button size="small" @click="viewTaskDetail(task.id)">查看详情</el-button>
            </div>
          </el-card>
          <div v-if="tasksList.pending.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无待处理任务" />
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="处理中" name="processing">
        <div v-loading="loading" class="tasks-grid">
          <el-card 
            v-for="(task, index) in tasksList.processing" 
            :key="task.id"
            class="task-card"
            shadow="hover"
          >
            <div class="task-header">
              <div class="task-number">#{{ index + 1 }}</div>
              <el-tag type="warning">处理中</el-tag>
            </div>
            <div class="task-title">{{ task.title }}</div>
            <div class="task-info">
              <div class="info-item">
                <span class="info-label">行政区：</span>
                <span class="info-value">{{ task.district }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">更新时间：</span>
                <span class="info-value">{{ task.updateTime }}</span>
              </div>
            </div>
            <div class="task-actions">
              <el-button size="small" type="warning" @click="updateTaskProgress(task.id)">更新进展</el-button>
              <el-button size="small" type="success" @click="completeTask(task.id)">完成任务</el-button>
              <el-button size="small" @click="viewTaskDetail(task.id)">查看详情</el-button>
            </div>
          </el-card>
          <div v-if="tasksList.processing.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无处理中任务" />
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="已完成" name="completed">
        <div v-loading="loading" class="tasks-grid">
          <el-card 
            v-for="(task, index) in tasksList.completed" 
            :key="task.id"
            class="task-card"
            shadow="hover"
          >
            <div class="task-header">
              <div class="task-number">#{{ index + 1 }}</div>
              <el-tag type="success">已完成</el-tag>
            </div>
            <div class="task-title">{{ task.title }}</div>
            <div class="task-info">
              <div class="info-item">
                <span class="info-label">行政区：</span>
                <span class="info-value">{{ task.district }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">完成时间：</span>
                <span class="info-value">{{ task.updateTime }}</span>
              </div>
            </div>
            <div class="task-actions">
              <el-button size="small" @click="viewTaskDetail(task.id)">查看详情</el-button>
            </div>
          </el-card>
          <div v-if="tasksList.completed.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无已完成任务" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 更新进展对话框 -->
    <el-dialog
      v-model="updateDialogVisible"
      title="更新处理进展"
      width="500px"
    >
      <el-form :model="updateForm" :rules="updateRules" ref="updateFormRef">
        <el-form-item prop="remark" label="处理备注">
          <el-input
            type="textarea"
            v-model="updateForm.remark"
            placeholder="请输入处理进展..."
            :rows="4"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="updateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpdateProgress" :loading="submitting">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('pending')
const loading = ref(false)
const submitting = ref(false)
const updateDialogVisible = ref(false)
const updateFormRef = ref()
const currentTaskId = ref(null)

const tasksList = reactive({
  pending: [],
  processing: [],
  completed: []
})

const updateForm = reactive({
  remark: ''
})

const updateRules = {
  remark: [
    { required: true, message: '请输入处理备注', trigger: 'blur' },
    { min: 5, message: '备注长度至少5个字符', trigger: 'blur' }
  ]
}

const fetchTasks = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/worker/tasks', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      const list = data.data.records || []
      
      // 清空所有列表
      Object.keys(tasksList).forEach(key => {
        tasksList[key] = []
      })
      
      // 分类任务
      list.forEach(task => {
        if (task.status === 'PENDING' || task.status === 'ASSIGNED') {
          tasksList.pending.push(task)
        } else if (task.status === 'PROCESSING') {
          tasksList.processing.push(task)
        } else if (task.status === 'RESOLVED' || task.status === 'CLOSED') {
          tasksList.completed.push(task)
        }
      })
    } else {
      ElMessage.error(data.message || '获取任务列表失败')
    }
  } catch (error) {
    console.error('获取任务列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // 切换标签时不需要重新获取数据，因为已经在加载时分类了
}

const startTask = async (taskId) => {
  try {
    const response = await fetch(`/api/worker/task/${taskId}/start`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ remark: '开始处理任务' })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('任务开始处理')
      // 重新获取任务列表
      fetchTasks()
    } else {
      ElMessage.error(data.message || '开始处理任务失败')
    }
  } catch (error) {
    console.error('开始处理任务错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

const updateTaskProgress = (taskId) => {
  currentTaskId.value = taskId
  updateForm.remark = ''
  updateDialogVisible.value = true
}

const submitUpdateProgress = async () => {
  if (!updateFormRef.value) return
  
  await updateFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await fetch(`/api/worker/task/${currentTaskId.value}/update`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ remark: updateForm.remark })
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          ElMessage.success('处理进展已更新')
          updateDialogVisible.value = false
          // 重新获取任务列表
          fetchTasks()
        } else {
          ElMessage.error(data.message || '更新处理进展失败')
        }
      } catch (error) {
        console.error('更新处理进展错误:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

const completeTask = async (taskId) => {
  try {
    await ElMessageBox.confirm('确定要完成此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/worker/task/${taskId}/resolve`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ remark: '任务处理完成' })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('任务处理完成')
      // 重新获取任务列表
      fetchTasks()
    } else {
      ElMessage.error(data.message || '完成任务失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成任务错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const viewTaskDetail = (taskId) => {
  router.push(`/app/complaint/detail/${taskId}`)
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.tasks-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.task-card {
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-number {
  font-size: 14px;
  font-weight: 500;
  color: #909399;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
  word-break: break-word;
}

.task-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  width: 80px;
  color: #909399;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
  word-break: break-word;
}

.task-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
  
  /* PC端按钮布局 */
  @media (min-width: 769px) {
    .task-actions {
      flex-direction: row;
      justify-content: flex-end;
      gap: 10px;
    }
    
    .task-actions .el-button {
      width: auto !important;
      margin-bottom: 0 !important;
    }
  }

.empty-state {
  grid-column: 1 / -1;
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  text-align: right;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .tasks-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .tasks-container {
    padding: 15px;
    min-height: calc(100vh - 50px);
  }
  
  .tasks-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .task-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .task-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .info-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .info-label {
    width: auto;
  }
  
  /* 确保按钮样式一致 */
  .task-actions .el-button {
    width: 100% !important;
    padding: 10px 16px !important;
    font-size: 14px !important;
    box-sizing: border-box !important;
    margin: 0 !important;
    border-radius: 4px !important;
  }
  
  .task-actions .el-button + .el-button {
    margin-top: 8px !important;
  }
}

@media (max-width: 480px) {
  .tasks-container {
    padding: 10px;
  }
  
  h2 {
    font-size: 18px;
  }
  
  .task-title {
    font-size: 14px;
  }
  
  .info-item {
    font-size: 13px;
  }
}
</style>