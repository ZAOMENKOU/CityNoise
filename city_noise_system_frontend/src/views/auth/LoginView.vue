<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>城市噪音监测与投诉系统</h1>
        <p>请登录以继续</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="loginForm" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="user"
          ></el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="lock"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn" :loading="loading">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2026 城市噪音监测与投诉系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loginForm = ref()

const handleLogin = async () => {
  if (!loginForm.value) return
  
  loginForm.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // 调用后端登录API
      fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: form.value.username,
          password: form.value.password
        })
      })
      .then(response => response.json())
      .then(data => {
        if (data.code === 200) {
          // 登录成功，保存token
          localStorage.setItem('token', data.data.token)
          localStorage.setItem('userInfo', JSON.stringify(data.data.userInfo))
          
          ElMessage.success('登录成功！')
          // 根据用户角色跳转到不同页面
          const userRole = data.data.userInfo.role
          if (userRole === 'ADMIN') {
            // 管理员跳转到仪表盘
            router.push('/app/dashboard')
          } else if (userRole === 'WORKER') {
            // 处理人员跳转到我的任务页面
            router.push('/app/worker/tasks')
          } else if (userRole === 'RESIDENT') {
            // 居民跳转到投诉提交页面
            router.push('/app/complaint/submit')
          } else {
            // 默认跳转到仪表盘
            router.push('/app/dashboard')
          }
        } else {
          ElMessage.error(data.message || '登录失败')
        }
      })
      .catch(error => {
        console.error('登录错误:', error)
        ElMessage.error('网络错误，请稍后重试')
      })
      .finally(() => {
        loading.value = false
      })
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, #2575fc 100%);
  padding: var(--spacing-md);
  animation: gradientShift 10s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: var(--bg-primary);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-xl);
  transition: all var(--transition-base);
  animation: slideUp 0.5s ease-out;
}

.login-box:hover {
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  transform: translateY(-4px);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.login-header h1 {
  color: var(--text-primary);
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-md);
  font-weight: var(--font-weight-bold);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.login-header h1::before,
.login-header h1::after {
  content: '';
  width: 24px;
  height: 24px;
  background-color: var(--primary-color);
  border-radius: var(--border-radius-full);
  opacity: 0.8;
}

.login-header p {
  color: var(--text-tertiary);
  font-size: var(--font-size-base);
  margin: 0;
}

.login-form {
  margin-top: var(--spacing-lg);
}

.login-form .el-form-item {
  margin-bottom: var(--spacing-lg);
}

.login-form .el-input {
  height: 52px;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.login-form .el-input__inner {
  height: 52px;
  font-size: var(--font-size-base);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-primary);
  transition: all var(--transition-fast);
}

.login-form .el-input__inner:hover {
  border-color: var(--primary-hover);
}

.login-form .el-input__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(51, 102, 255, 0.1);
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: var(--font-size-base);
  border-radius: var(--border-radius-lg);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.login-btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(51, 102, 255, 0.3);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 4px 8px rgba(51, 102, 255, 0.3);
}

.login-footer {
  text-align: center;
  margin-top: var(--spacing-2xl);
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: var(--spacing-sm);
  }
  
  .login-box {
    padding: var(--spacing-2xl);
    box-shadow: var(--shadow-lg);
  }
  
  .login-header h1 {
    font-size: var(--font-size-xl);
  }
  
  .login-header h1::before,
  .login-header h1::after {
    width: 20px;
    height: 20px;
  }
  
  .login-form .el-input {
    height: 48px;
  }
  
  .login-form .el-input__inner {
    height: 48px;
    font-size: var(--font-size-sm);
  }
  
  .login-btn {
    height: 48px;
    font-size: var(--font-size-sm);
  }
  
  .login-footer {
    margin-top: var(--spacing-xl);
    font-size: var(--font-size-xs);
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: var(--spacing-lg);
  }
  
  .login-header h1 {
    font-size: var(--font-size-lg);
  }
  
  .login-header p {
    font-size: var(--font-size-sm);
  }
  
  .login-form .el-form-item {
    margin-bottom: var(--spacing-md);
  }
  
  .login-form .el-input {
    height: 44px;
  }
  
  .login-form .el-input__inner {
    height: 44px;
  }
  
  .login-btn {
    height: 44px;
  }
}

/* 加载动画 */
.login-btn.is-loading {
  transform: none;
  box-shadow: none;
}

/* 错误状态 */
.login-form .el-form-item.is-error .el-input__inner {
  border-color: var(--danger-color);
}

.login-form .el-form-item.is-error .el-input__inner:focus {
  box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.1);
}

/* 禁用状态 */
.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>