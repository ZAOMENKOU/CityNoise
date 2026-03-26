<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>城市噪音监测与投诉系统</h1>
        <p>注册新账号</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="registerForm" class="register-form">
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
        
        <el-form-item prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名" 
            prefix-icon="avatar"
          ></el-input>
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号码" 
            prefix-icon="phone"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="register-btn" :loading="loading">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>已有账号？<a href="/login" class="login-link">立即登录</a></p>
        <p>© 2024 城市噪音监测与投诉系统</p>
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
  password: '',
  realName: '',
  phone: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在 3-20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6-20 个字符之间', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度应在 2-20 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: false, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const registerForm = ref()

const handleRegister = async () => {
  if (!registerForm.value) return
  
  await registerForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用后端注册API
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: form.value.username,
            password: form.value.password,
            realName: form.value.realName,
            phone: form.value.phone
          })
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          // 注册成功，保存token
          localStorage.setItem('token', data.data.token)
          localStorage.setItem('userInfo', JSON.stringify(data.data.userInfo))
          
          ElMessage.success('注册成功！')
          // 跳转到仪表盘
          router.push('/app/dashboard')
        } else {
          ElMessage.error(data.message || '注册失败')
        }
      } catch (error) {
        console.error('注册错误:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
}

.register-box {
  width: 400px;
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #303133;
  font-size: 24px;
  margin-bottom: 10px;
}

.register-header p {
  color: #909399;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  margin-top: 30px;
}

.register-footer p {
  color: #909399;
  font-size: 12px;
  margin-bottom: 8px;
}

.login-link {
  color: #409eff;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}
</style>