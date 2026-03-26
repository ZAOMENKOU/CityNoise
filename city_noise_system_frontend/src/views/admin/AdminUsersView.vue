<template>
  <div class="users-container">
    <h2>用户管理</h2>
    <el-button type="primary" @click="handleAddUser" style="margin-bottom: 20px;">添加用户</el-button>
    
    <!-- 卡片式用户列表 -->
    <div class="users-grid" v-loading="loading">
      <el-card
        v-for="user in usersData.records"
        :key="user.id"
        shadow="hover"
        class="user-card"
        :class="{ 'deleted-user': user.deleted === 1 }"
      >
        <div class="user-card-header">
          <h3 class="user-name">{{ user.realName }}</h3>
          <div style="display: flex; gap: 8px;">
            <el-tag :type="getRoleType(user.role)">{{ getRoleText(user.role) }}</el-tag>
            <el-tag v-if="user.deleted === 1" type="info" effect="plain">已删除</el-tag>
          </div>
        </div>
        <div class="user-card-body">
          <div class="user-info-item">
            <span class="label">用户名:</span>
            <span class="value">{{ user.username }}</span>
          </div>
          <div class="user-info-item">
            <span class="label">手机号:</span>
            <span class="value">{{ user.phone }}</span>
          </div>
          <div class="user-info-item">
            <span class="label">注册时间:</span>
            <span class="value">{{ formatDate(user.createTime) }}</span>
          </div>
        </div>
        <div class="user-card-footer">
          <el-button size="small" @click="handleEditUser(user)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteUser(user.id)">删除</el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 无数据提示 -->
    <div v-if="!loading && usersData.records.length === 0" class="empty-state">
      <el-empty description="暂无用户数据" />
    </div>
    
    <div style="margin-top: 20px;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="usersData.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" class="custom-form">
        <el-form-item prop="username" label="用户名">
          <el-input v-model="userForm.username" :disabled="isEdit" class="form-control"></el-input>
        </el-form-item>
        <el-form-item prop="password" label="密码" :required="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="添加用户时必填，编辑时可选" class="form-control"></el-input>
        </el-form-item>
        <el-form-item prop="realName" label="真实姓名">
          <el-input v-model="userForm.realName" class="form-control"></el-input>
        </el-form-item>
        <el-form-item prop="role" label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" class="form-control">
            <el-option label="居民" value="RESIDENT"></el-option>
            <el-option label="处理人员" value="WORKER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="userForm.phone" class="form-control"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="submitting">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const userDialogVisible = ref(false)
const userFormRef = ref()
const isEdit = ref(false)
const currentUserId = ref(null)

const usersData = ref({
  records: [],
  list: [],
  total: 0,
  current: 1,
  size: 10,
  pages: 1
})

const userForm = ref({
  username: '',
  password: '',
  realName: '',
  role: 'RESIDENT',
  phone: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, callback) => {
      if (isEdit.value) {
        callback()
      } else if (!value) {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度至少6个字符'))
      } else {
        callback()
      }
    }}
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const getRoleText = (role) => {
  const roleMap = {
    'RESIDENT': '居民',
    'WORKER': '处理人员',
    'ADMIN': '管理员',
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const roleTypeMap = {
    'RESIDENT': 'info',
    'WORKER': 'warning',
    'ADMIN': 'danger',
    'SUPERVISOR': 'success'
  }
  return roleTypeMap[role] || ''
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/admin/users?page=${currentPage.value}&size=${pageSize.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      usersData.value = {
        ...data.data,
        records: data.data.records || data.data.list || []
      }
    } else {
      ElMessage.error(data.message || '获取用户列表失败')
      // 确保 records 始终是数组
      usersData.value.records = []
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    // 确保 records 始终是数组
    usersData.value.records = []
  } finally {
    loading.value = false
  }
}

const handleAddUser = () => {
  isEdit.value = false
  currentUserId.value = null
  userForm.value = {
    username: '',
    password: '',
    realName: '',
    role: 'RESIDENT',
    phone: ''
  }
  userDialogVisible.value = true
}

const handleEditUser = (user) => {
  isEdit.value = true
  currentUserId.value = user.id
  userForm.value = {
    username: user.username,
    password: '',
    realName: user.realName,
    role: user.role,
    phone: user.phone
  }
  userDialogVisible.value = true
}

const handleDeleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    })
    
    const response = await fetch(`/api/admin/users/${userId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('用户删除成功')
      fetchUsers()
    } else {
      ElMessage.error(data.message || '用户删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const submitUserForm = async () => {
  if (!userFormRef.value) return
  
  console.log('提交的用户数据:', userForm.value);
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let response
        if (isEdit.value) {
          // 编辑用户
          response = await fetch(`/api/admin/users/${currentUserId.value}`, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(userForm.value)
          })
        } else {
          // 添加用户
          response = await fetch('/api/admin/users', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(userForm.value)
          })
        }
        
        const data = await response.json()
        console.log('服务器响应:', data);
        
        if (data.code === 200) {
          ElMessage.success(isEdit.value ? '用户编辑成功' : '用户添加成功')
          userDialogVisible.value = false
          fetchUsers()
        } else {
          ElMessage.error(data.message || (isEdit.value ? '用户编辑失败' : '用户添加失败'))
        }
      } catch (error) {
        console.error(isEdit.value ? '编辑用户错误:' : '添加用户错误:', error)
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
  fetchUsers()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-container {
  padding: 20px;
}

/* 卡片网格布局 */
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

/* 用户卡片样式 */
.user-card {
  transition: all 0.3s ease;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

/* 已删除用户样式 */
.user-card.deleted-user {
  opacity: 0.6;
  background-color: #f5f7fa;
}

.user-card.deleted-user .user-name {
  text-decoration: line-through;
  color: #909399;
}

.user-card.deleted-user .user-info-item .value {
  color: #909399;
}

.user-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.user-card-body {
  margin-bottom: 16px;
}

.user-info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.user-info-item .label {
  width: 80px;
  color: #909399;
}

.user-info-item .value {
  flex: 1;
  color: #303133;
}

.user-card-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 自定义表单样式 */
.custom-form {
  width: 100%;
}

.form-control {
  width: 100%;
  font-size: 14px;
}

.el-select .el-input__inner {
  padding: 10px;
  font-size: 14px;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .users-container {
    padding: 10px;
  }
  
  /* 移动端单列布局 */
  .users-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  /* 调整卡片样式 */
  .user-card {
    padding: 15px;
  }
  
  .user-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .user-name {
    font-size: 16px;
  }
  
  .user-info-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .user-info-item .label {
    width: auto;
    font-size: 12px;
  }
  
  .user-info-item .value {
    font-size: 14px;
  }
  
  .user-card-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .user-card-footer .el-button {
    width: 100%;
  }
  
  /* 调整对话框宽度 */
  .el-dialog {
    width: 90% !important;
    max-width: 500px;
  }
  
  /* 调整表单元素大小 */
  .el-form-item {
    margin-bottom: 15px;
  }
  
  /* 调整按钮大小和间距 */
  .el-button {
    font-size: 14px;
    padding: 8px 12px;
  }
  
  /* 调整分页控件 */
  .el-pagination {
    font-size: 12px;
  }
  
  .el-pagination__sizes {
    display: none;
  }
}

/* 平板端样式 */
@media (min-width: 769px) and (max-width: 1024px) {
  .users-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 15px;
  }
}
</style>