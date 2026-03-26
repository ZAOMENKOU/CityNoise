<template>
  <div class="task-monitor-container">
    <h2>任务监控</h2>
    
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>筛选条件</span>
        </div>
      </template>
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态">
            <el-option label="全部" value=""></el-option>
            <el-option label="待处理" value="PENDING"></el-option>
            <el-option label="已分配" value="ASSIGNED"></el-option>
            <el-option label="处理中" value="PROCESSING"></el-option>
            <el-option label="已解决" value="RESOLVED"></el-option>
            <el-option label="已关闭" value="CLOSED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理人员">
          <el-select v-model="filterForm.workerId" placeholder="选择处理人员">
            <el-option label="全部" value=""></el-option>
            <el-option v-for="worker in workers" :key="worker.id" :label="worker.realName" :value="worker.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchTasks">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-table :data="tasks" style="width: 100%" v-loading="loading">
      <el-table-column label="任务编号" width="100">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="任务标题" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="district" label="行政区" width="150"></el-table-column>
      <el-table-column prop="userRealName" label="投诉人" width="120"></el-table-column>
      <el-table-column prop="workerRealName" label="处理人员" width="120"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="viewTaskDetail(scope.row.id)">查看详情</el-button>
          <el-button size="small" type="primary" @click="modifyTaskProgress(scope.row.id)">修改进度</el-button>
        </template>
      </el-table-column>
    </el-table>
    
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
    
    <!-- 修改进度对话框 -->
    <el-dialog
      v-model="modifyDialogVisible"
      title="修改任务进度"
      width="500px"
    >
      <el-form :model="modifyForm" :rules="modifyRules" ref="modifyFormRef">
        <el-form-item prop="status" label="目标状态">
          <el-select v-model="modifyForm.status" placeholder="选择状态">
            <el-option label="待处理" value="PENDING"></el-option>
            <el-option label="已分配" value="ASSIGNED"></el-option>
            <el-option label="处理中" value="PROCESSING"></el-option>
            <el-option label="已解决" value="RESOLVED"></el-option>
            <el-option label="已关闭" value="CLOSED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="remark" label="修改原因">
          <el-input
            type="textarea"
            v-model="modifyForm.remark"
            placeholder="请输入修改原因..."
            :rows="4"
          ></el-input>
        </el-form-item>
        <el-form-item prop="workerId" label="处理人员">
          <el-select v-model="modifyForm.workerId" placeholder="选择处理人员（可选）">
            <el-option label="不修改" value=""></el-option>
            <el-option v-for="worker in workers" :key="worker.id" :label="worker.realName" :value="worker.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="modifyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitModifyProgress" :loading="submitting">
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
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const modifyDialogVisible = ref(false)
const modifyFormRef = ref()
const currentTaskId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tasks = ref([])
const workers = ref([])
const filterForm = reactive({
  status: '',
  workerId: ''
})

const modifyForm = reactive({
  status: '',
  remark: '',
  workerId: ''
})

const modifyRules = {
  status: [
    { required: true, message: '请选择目标状态', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入修改原因', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': '',
    'ASSIGNED': 'info',
    'PROCESSING': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'danger'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'ASSIGNED': '已分配',
    'PROCESSING': '处理中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || status
}

const fetchTasks = async () => {
  loading.value = true
  try {
    let url = `/api/admin/tasks/monitor?page=${currentPage.value}&size=${pageSize.value}`
    if (filterForm.status) {
      url += `&status=${filterForm.status}`
    }
    if (filterForm.workerId) {
      url += `&workerId=${filterForm.workerId}`
    }
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      tasks.value = data.data.records || []
      total.value = data.data.total || 0
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

const resetFilter = () => {
  filterForm.status = ''
  filterForm.workerId = ''
  currentPage.value = 1
  fetchTasks()
}

const viewTaskDetail = (taskId) => {
  router.push(`/app/complaint/detail/${taskId}`)
}

const modifyTaskProgress = (taskId) => {
  currentTaskId.value = taskId
  modifyForm.status = ''
  modifyForm.remark = ''
  modifyForm.workerId = ''
  modifyDialogVisible.value = true
}

const fetchWorkers = async () => {
  try {
    const response = await fetch('/api/admin/workers', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      workers.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取处理人员列表失败')
    }
  } catch (error) {
    console.error('获取处理人员列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

const submitModifyProgress = async () => {
  if (!modifyFormRef.value) return
  
  await modifyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const requestData = {
          status: modifyForm.status,
          remark: modifyForm.remark
        }
        
        if (modifyForm.workerId) {
          requestData.workerId = modifyForm.workerId
        }
        
        const response = await fetch(`/api/admin/task/${currentTaskId.value}/progress`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestData)
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          ElMessage.success('任务进度修改成功')
          modifyDialogVisible.value = false
          // 重新获取任务列表
          fetchTasks()
        } else {
          ElMessage.error(data.message || '修改任务进度失败')
        }
      } catch (error) {
        console.error('修改任务进度错误:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTasks()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchTasks()
}

onMounted(() => {
  fetchWorkers()
  fetchTasks()
})
</script>

<style scoped>
.task-monitor-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 0;
  width: 100%;
}

.filter-form .el-form-item {
  margin-right: 15px;
  margin-bottom: 10px;
}

.filter-form .el-select {
  min-width: 120px;
  width: 100%;
  max-width: 200px;
}

.dialog-footer {
  text-align: right;
}
</style>