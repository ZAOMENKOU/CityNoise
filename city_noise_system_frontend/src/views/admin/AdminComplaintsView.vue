<template>
  <div class="complaints-container">
    <h2>投诉管理</h2>
    <el-table :data="complaints.records" style="width: 100%" v-loading="loading">
      <el-table-column label="编号" width="80">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="district" label="行政区" width="150"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column prop="userRealName" label="用户" width="120"></el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row.id)">查看</el-button>
          <el-button size="small" type="primary" @click="handleProcess(scope.row.id)">处理</el-button>
          <el-button size="small" type="info" @click="handleTaskMonitor">任务监控</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div style="margin-top: 20px;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="complaints.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 处理投诉对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理投诉"
      width="500px"
    >
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" class="custom-form">
        <el-form-item label="投诉标题" disabled>
          <el-input v-model="processForm.title"></el-input>
        </el-form-item>
        <el-form-item prop="workerId" label="分配给">
          <el-select v-model="processForm.workerId" placeholder="请选择处理人员">
            <el-option
              v-for="worker in workers"
              :key="worker.id"
              :label="worker.realName"
              :value="worker.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="remark" label="备注">
          <el-input
            type="textarea"
            v-model="processForm.remark"
            placeholder="请输入处理备注"
            :rows="3"
            resize="vertical"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess" :loading="submitting">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const processDialogVisible = ref(false)
const processFormRef = ref()
const currentComplaintId = ref(null)

const complaints = ref({
  list: [],
  total: 0,
  current: 1,
  size: 10,
  pages: 1
})

const workers = ref([])
const processForm = ref({
  title: '',
  workerId: null,
  remark: ''
})

const processRules = {
  workerId: [
    { required: true, message: '请选择处理人员', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请填写处理备注', trigger: 'blur' }
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

const fetchComplaints = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/admin/complaints?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      complaints.value = data.data
    } else {
      ElMessage.error(data.message || '获取投诉列表失败')
    }
  } catch (error) {
    console.error('获取投诉列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchWorkers = async () => {
  try {
    console.log('开始获取处理人员列表...')
    const response = await fetch('/api/admin/users?roleFilter=WORKER', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    console.log('获取处理人员列表响应:', data)
    
    if (data.code === 200) {
      // 手动过滤只保留WORKER角色的用户
      workers.value = (data.data.records || []).filter(user => user.role === 'WORKER')
      console.log('处理人员列表:', workers.value)
    } else {
      ElMessage.error(data.message || '获取处理人员列表失败')
    }
  } catch (error) {
    console.error('获取处理人员列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

const handleView = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}

const handleTaskMonitor = () => {
  router.push('/app/admin/task-monitor')
}

const handleProcess = (id) => {
  currentComplaintId.value = id
  const complaint = complaints.value.records.find(item => item.id === id)
  if (complaint) {
    processForm.value.title = complaint.title
    processForm.value.workerId = null
    processForm.value.remark = ''
  }
  fetchWorkers()
  processDialogVisible.value = true
}

const submitProcess = async () => {
  if (!processFormRef.value) return
  
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await fetch(`/api/admin/complaint/${currentComplaintId.value}/assign?workerId=${processForm.value.workerId}`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          ElMessage.success('任务分配成功')
          processDialogVisible.value = false
          fetchComplaints()
        } else {
          ElMessage.error(data.message || '任务分配失败')
        }
      } catch (error) {
        console.error('任务分配错误:', error)
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
  fetchComplaints()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchComplaints()
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped>
.complaints-container {
  padding: 20px;
}

/* 自定义表单样式 */
.custom-form {
  width: 100%;
}
</style>