<template>
  <div class="complaint-list-container">
    <h2>投诉列表</h2>
    <el-button type="primary" @click="goToSubmit" style="margin-bottom: 20px;">提交新投诉</el-button>
    
    <div v-loading="loading" class="complaint-cards">
      <el-card 
        v-for="(complaint, index) in complaints" 
        :key="complaint.id"
        class="complaint-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="card-title">
            <span class="complaint-number">#{{ (currentPage - 1) * pageSize + index + 1 }}</span>
            <h3>{{ complaint.title }}</h3>
          </div>
          <StatusTag :status="complaint.status" />
        </div>
        <div class="card-body">
          <div class="card-info">
            <span class="info-item">
              <i class="el-icon-map-location"></i>
              {{ complaint.district }}
            </span>
            <span class="info-item">
              <i class="el-icon-time"></i>
              {{ complaint.createTime }}
            </span>
          </div>
        </div>
        <div class="card-footer">
          <el-button 
            type="primary" 
            size="small" 
            @click="viewDetail(complaint.id)"
          >
            查看详情
          </el-button>
          <el-button 
            v-if="complaint.status === 'PENDING' && (currentUser.role === 'RESIDENT' || currentUser.role === 'ADMIN')" 
            type="danger" 
            size="small" 
            style="margin-left: 10px;"
            @click="showDeleteDialog(complaint.id, complaint.title)"
          >
            删除投诉
          </el-button>
        </div>
      </el-card>
      
      <!-- 空状态 -->
      <div v-if="complaints.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无投诉记录" />
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
    
    <!-- 删除投诉对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除投诉"
      width="400px"
    >
      <el-form :model="deleteForm" label-width="80px">
        <el-form-item label="删除原因">
          <el-input
            v-model="deleteForm.reason"
            type="textarea"
            placeholder="请输入删除投诉的原因"
            :rows="3"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteComplaint">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/business/StatusTag.vue'

const router = useRouter()

const complaints = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentUser = ref({ id: null, role: '' })

// 删除投诉相关
const deleteDialogVisible = ref(false)
const currentComplaintId = ref(null)
const deleteForm = ref({
  reason: ''
})

const fetchComplaints = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/complaint/my?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      complaints.value = data.data.records
      total.value = data.data.total
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

const goToSubmit = () => {
  router.push('/app/complaint/submit')
}

const viewDetail = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}

const showDeleteDialog = (id, title) => {
  currentComplaintId.value = id
  deleteForm.value.reason = ''
  deleteDialogVisible.value = true
}

const deleteComplaint = async () => {
  if (!deleteForm.value.reason.trim()) {
    ElMessage.error('请输入删除原因')
    return
  }
  
  try {
    const response = await fetch(`/api/complaint/${currentComplaintId.value}/delete`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reason: deleteForm.value.reason
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('投诉已成功删除')
      deleteDialogVisible.value = false
      fetchComplaints()
    } else {
      ElMessage.error(data.message || '删除投诉失败')
    }
  } catch (error) {
    console.error('删除投诉错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
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

// 获取当前用户信息
const fetchCurrentUser = async () => {
  try {
    const response = await fetch('/api/auth/me', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      currentUser.value = data.data
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
  }
}

onMounted(async () => {
  await fetchCurrentUser()
  fetchComplaints()
})
</script>

<style scoped>
.complaint-list-container {
  padding: 20px;
}

.complaint-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.complaint-card {
  transition: all 0.3s ease;
}

.complaint-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.card-title {
  flex: 1;
  margin-right: 15px;
}

.complaint-number {
  display: inline-block;
  background-color: #f0f2f5;
  color: #909399;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-bottom: 8px;
}

.card-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.card-body {
  margin-bottom: 15px;
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.info-item i {
  margin-right: 5px;
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .complaint-list-container {
    padding: 10px;
  }
  
  .complaint-cards {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .card-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .card-title h3 {
    font-size: 15px;
  }
  
  .info-item {
    font-size: 13px;
  }
  
  .el-button {
    font-size: 13px;
    padding: 8px 16px;
  }
}

@media (max-width: 480px) {
  .card-title h3 {
    font-size: 14px;
  }
  
  .info-item {
    font-size: 12px;
  }
  
  .el-button {
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>