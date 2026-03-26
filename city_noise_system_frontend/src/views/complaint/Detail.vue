<template>
  <div class="complaint-detail-container">
    <h2>投诉详情</h2>
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>投诉信息 #{{ complaint.id }}</span>
          <el-tag :type="getStatusType(complaint.status)">{{ getStatusText(complaint.status) }}</el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题">{{ complaint.title }}</el-descriptions-item>
        <el-descriptions-item label="噪音类型">{{ getNoiseTypeText(complaint.noiseType) }}</el-descriptions-item>
        <el-descriptions-item label="行政区">{{ complaint.location?.district || complaint.district }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ complaint.location?.detailAddress || complaint.detailAddress }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ complaint.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ complaint.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">{{ complaint.description }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 图片展示 -->
      <div v-if="complaint.images && complaint.images.length > 0" style="margin-top: 20px;">
        <h3>上传图片</h3>
        <div class="image-list">
          <el-image
            v-for="(image, index) in complaint.images"
            :key="index"
            :src="getImageUrl(image)"
            fit="cover"
            class="detail-image"
            :preview-src-list="complaint.images.map(img => getImageUrl(img))"
            @error="handleImageError(index)"
          >
            <template #error>
              <div class="image-error">
                加载失败
              </div>
            </template>
          </el-image>
        </div>
      </div>
      
      <!-- 处理记录 -->
      <div v-if="complaint.handlingRecords && complaint.handlingRecords.length > 0" style="margin-top: 20px;">
        <h3>处理记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in complaint.handlingRecords"
            :key="index"
            :timestamp="record.createTime"
            :type="getRecordType(record.action)"
          >
            <el-card>
              <h4>{{ getActionText(record.action) }}</h4>
              <p>{{ record.remark }}</p>
              <p class="handler-info">处理人：{{ record.handlerName }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <!-- 评价表单 -->
      <div v-if="canRate" style="margin-top: 20px;">
        <h3>评价处理结果</h3>
        <el-form :model="ratingForm" :rules="ratingRules" ref="ratingFormRef">
          <el-form-item prop="score" label="评分">
            <el-rate v-model="ratingForm.score" show-score></el-rate>
          </el-form-item>
          <el-form-item prop="comment" label="评价内容">
            <el-input
              type="textarea"
              v-model="ratingForm.comment"
              placeholder="请输入您的评价..."
              :rows="3"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitRating" :loading="submitting">提交评价</el-button>
            <el-button @click="cancelRating">取消</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 问题未解决选项 -->
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0;">
          <h3>问题未解决</h3>
          <p>如果处理后问题仍然存在，请选择以下选项：</p>
          <el-form :model="unsolvedForm" ref="unsolvedFormRef">
            <el-form-item label="问题原因">
              <el-input
                type="textarea"
                v-model="unsolvedForm.reason"
                placeholder="请详细描述问题仍然存在的原因..."
                :rows="4"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="submitUnsolved" :loading="submitting">标记为未解决</el-button>
              <el-button @click="cancelUnsolved">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <div style="margin-top: 20px;">
        <!-- 联系投诉人按钮，只对处理人员显示 -->
        <el-button v-if="canContact" type="success" @click="contactComplainant">联系投诉人</el-button>
        <!-- 上传图片按钮，只对投诉人显示 -->
        <el-button v-if="canUpload" type="primary" @click="openUploadDialog">
          <el-icon><upload /></el-icon>
          上传图片
        </el-button>
        <!-- 删除投诉按钮，只对居民或管理员且状态为待处理的投诉显示 -->
        <el-button v-if="canCancel" type="danger" @click="showDeleteDialog">删除投诉</el-button>
        <el-button @click="goBack">返回列表</el-button>
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
            <el-button type="danger" @click="deleteComplaint" :loading="submitting">确认删除</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 联系投诉人对话框 -->
      <el-dialog
        v-model="contactDialogVisible"
        title="联系投诉人"
        width="400px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投诉人">
            {{ complaint.userRealName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ complaint.userPhone || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="投诉时间">
            {{ complaint.createTime }}
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="makePhoneCall">拨打电话</el-button>
          <el-button type="info" @click="sendMessage">发送短信</el-button>
          <el-button @click="contactDialogVisible = false">取消</el-button>
        </div>
      </el-dialog>
      
      <!-- 上传图片对话框 -->
      <el-dialog
        v-model="uploadDialogVisible"
        title="上传图片"
        width="500px"
      >
        <el-upload
          class="upload-area"
          action="#"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="uploadFileList"
          :auto-upload="false"
          multiple
          accept=".jpg,.jpeg,.png,.gif,.webp"
          drag
        >
          <el-icon class="upload-icon"><camera /></el-icon>
          <div class="upload-text">
            <div>点击或拖拽图片到此处上传</div>
            <div class="upload-hint">支持上传 JPG、JPEG、PNG、GIF、WEBP 格式文件，单个文件不超过 5MB，最多上传 5 个文件</div>
          </div>
        </el-upload>
        <div class="upload-preview" v-if="uploadFileList.length > 0">
          <div v-for="(file, index) in uploadFileList" :key="index" class="preview-item">
            <el-image
              :src="file.url || URL.createObjectURL(file.raw)"
              fit="cover"
              class="preview-image"
            ></el-image>
            <el-button 
              type="text" 
              class="preview-remove"
              @click="uploadFileList.splice(index, 1)"
            >
              <el-icon><delete /></el-icon>
            </el-button>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeUploadDialog">取消</el-button>
            <el-button type="primary" @click="uploadImages" :loading="uploading">
              上传图片
            </el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload, Camera } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const ratingFormRef = ref()
const complaint = ref({
  id: route.params.id || 1,
  title: '',
  noiseType: '',
  description: '',
  district: '',
  detailAddress: '',
  status: '',
  createTime: '',
  updateTime: '',
  images: [],
  handlingRecords: [],
  rating: null,
  userId: null, // 投诉人ID
  userRealName: '', // 投诉人姓名
  userPhone: '' // 投诉人电话
})

const ratingForm = ref({
  score: 5,
  comment: ''
})

const ratingRules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评价内容长度应在 1-500 个字符之间', trigger: 'blur' }
  ]
}

// 问题未解决表单
const unsolvedForm = ref({
  reason: ''
})

const unsolvedFormRef = ref()

const currentUser = ref({
  id: null,
  role: ''
})

const contactDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const uploadFileList = ref([])
const uploading = ref(false)

// 删除投诉表单
const deleteForm = ref({
  reason: ''
})

// 判断是否可以评价
const canRate = computed(() => {
  return complaint.value && 
         complaint.value.status === 'RESOLVED' && 
         currentUser.value && 
         currentUser.value.id === complaint.value.userId && 
         currentUser.value.role === 'RESIDENT'
})

// 判断是否可以联系投诉人
const canContact = computed(() => {
  return currentUser.value && 
         (currentUser.value.role === 'WORKER' || currentUser.value.role === 'ADMIN') && 
         complaint.value && 
         complaint.value.status !== 'CLOSED' && 
         complaint.value.userPhone
})

// 判断是否可以上传图片
const canUpload = computed(() => {
  console.log('canUpload check:', {
    currentUserId: currentUser.value?.id,
    complaintUserId: complaint.value?.userId,
    currentUserRole: currentUser.value?.role,
    complaintStatus: complaint.value?.status,
    isResident: currentUser.value?.role === 'RESIDENT',
    isPending: complaint.value?.status === 'PENDING',
    isAssigned: complaint.value?.status === 'ASSIGNED',
    isOwner: currentUser.value?.id == complaint.value?.userId
  })
  return complaint.value && 
         currentUser.value && 
         currentUser.value.id == complaint.value.userId && 
         currentUser.value.role === 'RESIDENT' && 
         (complaint.value.status === 'PENDING' || complaint.value.status === 'ASSIGNED')
})

// 判断是否可以删除投诉
const canCancel = computed(() => {
  return complaint.value && 
         currentUser.value && 
         (currentUser.value.role === 'ADMIN' || (currentUser.value.id == complaint.value.userId && currentUser.value.role === 'RESIDENT')) && 
         complaint.value.status === 'PENDING'
})

// 联系投诉人
const contactComplainant = () => {
  contactDialogVisible.value = true
}

// 拨打电话
const makePhoneCall = () => {
  if (complaint.value.userPhone) {
    // 这里只是模拟拨打电话，实际项目中可以使用tel:链接
    ElMessage.success(`正在拨打 ${complaint.value.userPhone}`)
    // 实际实现可以使用：window.location.href = `tel:${complaint.value.userPhone}`
  }
}

// 发送短信
const sendMessage = () => {
  if (complaint.value.userPhone) {
    // 这里只是模拟发送短信，实际项目中可以集成短信API
    ElMessage.success(`正在发送短信到 ${complaint.value.userPhone}`)
    // 实际实现需要调用后端短信API
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'ASSIGNED': 'info',
    'PROCESSING': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'danger',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'ASSIGNED': '已分配',
    'PROCESSING': '处理中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getNoiseTypeText = (noiseType) => {
  const noiseTypeMap = {
    'CONSTRUCTION': '施工噪音',
    'TRAFFIC': '交通噪音',
    'LIVING': '生活噪音',
    'COMMERCIAL': '商业噪音',
    'INDUSTRIAL': '工业噪音'
  }
  return noiseTypeMap[noiseType] || noiseType
}

const getRecordType = (action) => {
  const actionMap = {
    'SUBMIT': 'primary',
    'ASSIGN': 'info',
    'START_PROCESS': 'warning',
    'UPDATE_PROGRESS': 'warning',
    'RESOLVE': 'success',
    'CLOSE': 'danger',
    'RATE': 'success'
  }
  return actionMap[action] || ''
}

const getActionText = (action) => {
  const actionMap = {
    'SUBMIT': '投诉提交',
    'ASSIGN': '任务分配',
    'START_PROCESS': '开始处理',
    'UPDATE_PROGRESS': '更新进展',
    'RESOLVE': '处理完成',
    'CLOSE': '投诉关闭',
    'RATE': '用户评价'
  }
  return actionMap[action] || action
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

const fetchComplaintDetail = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/complaint/${route.params.id}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      complaint.value = data.data
      // 如果已经有评价，填充到评价表单中
      if (complaint.value.rating) {
        ratingForm.value.score = complaint.value.rating.score
        ratingForm.value.comment = complaint.value.rating.comment
      }
    } else {
      ElMessage.error(data.message || '获取投诉详情失败')
    }
  } catch (error) {
    console.error('获取投诉详情错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 提交评价
const submitRating = async () => {
  if (!ratingFormRef.value) return
  
  await ratingFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await fetch(`/api/complaint/${route.params.id}/rating`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(ratingForm.value)
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          ElMessage.success('评价成功')
          // 重新获取投诉详情
          await fetchComplaintDetail()
        } else {
          ElMessage.error(data.message || '评价失败')
        }
      } catch (error) {
        console.error('评价错误:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消评价
const cancelRating = () => {
  ratingForm.value.score = 5
  ratingForm.value.comment = ''
}

// 提交问题未解决
const submitUnsolved = async () => {
  if (!unsolvedFormRef.value) return
  
  // 简单验证
  if (!unsolvedForm.value.reason.trim()) {
    ElMessage.error('请输入问题原因')
    return
  }
  
  submitting.value = true
  try {
    const response = await fetch(`/api/complaint/${route.params.id}/unsolved`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(unsolvedForm.value)
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('已标记为未解决，处理人员将重新处理')
      // 重新获取投诉详情
      await fetchComplaintDetail()
      // 清空表单
      cancelUnsolved()
    } else {
      ElMessage.error(data.message || '标记未解决失败')
    }
  } catch (error) {
    console.error('标记未解决错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消标记未解决
const cancelUnsolved = () => {
  unsolvedForm.value.reason = ''
}

// 打开上传图片对话框
const openUploadDialog = () => {
  uploadFileList.value = []
  uploadDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file, fileList) => {
  // 检查文件数量
  if (fileList.length > 5) {
    ElMessage.error('最多上传 5 个文件')
    return false
  }
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('单个文件大小不能超过 5MB')
    return false
  }
  
  // 检查文件类型
  const allowedTypes = ['.jpg', '.jpeg', '.png', '.gif', '.webp']
  const fileExtension = file.name.substring(file.name.lastIndexOf('.'))
  if (!allowedTypes.includes(fileExtension.toLowerCase())) {
    ElMessage.error('只支持上传 JPG、JPEG、PNG、GIF、WEBP 格式文件')
    return false
  }
  
  // 更新文件列表
  uploadFileList.value = [...fileList]
}

// 处理文件移除
const handleFileRemove = (file, fileList) => {
  uploadFileList.value = [...fileList]
}

// 获取图片URL
const getImageUrl = (image) => {
  // 检查图片URL是否已经是完整的URL
  if (image.startsWith('http://') || image.startsWith('https://')) {
    return image
  }
  // 否则，添加后端服务的基础URL
  return `http://localhost:8080${image}`
}

// 处理图片加载错误
const handleImageError = (index) => {
  console.error(`图片加载失败，索引: ${index}`)
  console.error(`图片URL: ${getImageUrl(complaint.value.images[index])}`)
}

// 上传图片
const uploadImages = async () => {
  console.log('uploadImages 方法被调用')
  
  if (uploadFileList.value.length === 0) {
    console.log('没有选择要上传的图片')
    ElMessage.warning('请选择要上传的图片')
    return
  }
  
  if (uploading.value) {
    console.log('正在上传中，请勿重复操作')
    return
  }
  
  if (!complaint.value.id) {
    console.log('投诉ID不能为空')
    ElMessage.error('投诉ID不能为空')
    return
  }
  
  console.log('开始上传图片，投诉ID:', complaint.value.id)
  console.log('文件列表:', uploadFileList.value)
  
  uploading.value = true
  
  try {
    // 逐个上传图片
    const uploadPromises = uploadFileList.value.map(async (file, index) => {
      console.log(`开始上传第 ${index + 1} 个文件:`, file.name)
      
      const formData = new FormData()
      formData.append('file', file.raw)
      
      console.log('FormData 中文件:', formData.get('file'))
      
      const response = await fetch(`/api/complaint/${complaint.value.id}/upload-image`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      })
      
      console.log('上传响应状态:', response.status)
      console.log('上传响应状态文本:', response.statusText)
      
      const data = await response.json()
      console.log('上传响应数据:', data)
      
      if (data.code !== 200) {
        throw new Error(data.message || '图片上传失败')
      }
      
      console.log('图片上传成功，URL:', data.data.imageUrl)
      return data.data.imageUrl
    })
    
    // 等待所有图片上传完成
    console.log('等待所有图片上传完成')
    await Promise.all(uploadPromises)
    console.log('所有图片上传完成')
    
    // 先关闭对话框并清空文件列表，避免用户等待
    closeUploadDialog()
    console.log('上传对话框已关闭')
    
    // 然后重新获取投诉详情，更新图片列表
    console.log('重新获取投诉详情')
    await fetchComplaintDetail()
    console.log('投诉详情更新完成')
    
    // 最后显示成功消息
    ElMessage.success('图片上传成功')
    console.log('上传操作完成')
  } catch (error) {
    console.error('上传图片错误:', error)
    // 即使在错误情况下，也关闭对话框
    uploadDialogVisible.value = false
    console.log('上传错误，关闭对话框')
    ElMessage.error(error.message || '网络错误，请稍后重试')
  } finally {
    uploading.value = false
    console.log('上传操作结束')
  }
}

// 关闭上传图片对话框
const closeUploadDialog = () => {
  uploadDialogVisible.value = false
  uploadFileList.value = []
}

// 显示删除投诉对话框
const showDeleteDialog = () => {
  deleteForm.value.reason = ''
  deleteDialogVisible.value = true
}

// 删除投诉
const deleteComplaint = async () => {
  if (!deleteForm.value.reason.trim()) {
    ElMessage.error('请输入删除原因')
    return
  }
  
  submitting.value = true
  try {
    const response = await fetch(`/api/complaint/${complaint.value.id}/delete`, {
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
      // 跳转到投诉列表页面
      router.push('/app/complaint/list')
    } else {
      ElMessage.error(data.message || '删除投诉失败')
    }
  } catch (error) {
    console.error('删除投诉错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchComplaintDetail()
})
</script>

<style scoped>
.complaint-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.detail-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
}

.handler-info {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 上传图片样式 */
.upload-area {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.upload-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.preview-remove {
  position: absolute;
  top: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 4px;
  margin: 0;
  border-radius: 0 4px 0 4px;
}

.preview-remove:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>