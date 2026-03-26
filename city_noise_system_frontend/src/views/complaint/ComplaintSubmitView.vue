<template>
  <div class="page-container">
    <div class="content-wrapper">
      <h2 class="page-title">提交问题反馈</h2>
      
      <div class="main-container">
        <!-- 左侧地图区域 -->
        <div class="map-container-wrapper">
          <div class="map-header">
            <h3>地图选择位置</h3>
          </div>
          <div class="map-container" ref="mapContainer"></div>
          <div class="map-footer">
            <div class="coordinate-inputs">
              <el-input 
                v-model="latitudeInput" 
                placeholder="纬度" 
                size="small"
                @input="handleCoordinateInput"
              ></el-input>
              <el-input 
                v-model="longitudeInput" 
                placeholder="经度" 
                size="small"
                @input="handleCoordinateInput"
              ></el-input>
            </div>
            <div class="address-display">
              <div class="address-display-header">
                <span class="address-icon">📍</span>
                <span class="address-title">地址信息</span>
              </div>
              <div class="address-content">
                <div class="address-line">
                  <span class="address-label">详细地址：</span>
                  <span class="address-value">{{ selectedAddress }}</span>
                </div>
                <div class="address-line">
                  <span class="address-label">行政区划：</span>
                  <span class="address-value">{{ selectedDistrict || '未选择' }}</span>
                </div>
              </div>
            </div>
            <div class="map-tip">
              <span class="tip-icon">💡</span>
              <span>点击地图选择位置，或手动输入坐标</span>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="form-container">
          <div class="form-header">
            <h3>问题详情</h3>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="custom-form">
            <el-form-item label="噪音类型" prop="noiseType" class="form-item">
              <el-select v-model="form.noiseType" placeholder="请选择噪音类型">
                <el-option label="施工噪音" value="CONSTRUCTION"></el-option>
                <el-option label="交通噪音" value="TRAFFIC"></el-option>
                <el-option label="生活噪音" value="LIVING"></el-option>
                <el-option label="商业噪音" value="COMMERCIAL"></el-option>
                <el-option label="工业噪音" value="INDUSTRIAL"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="详细描述" prop="description" class="form-item">
              <el-input 
                type="textarea" 
                v-model="form.description" 
                placeholder="请详细描述问题..."
                :rows="4"
                maxlength="500"
                show-word-limit
                class="textarea"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="上传图片" prop="images" class="form-item">
              <el-upload
                class="upload-area"
                action="#"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :file-list="fileList"
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
              <div class="upload-preview" v-if="fileList.length > 0">
                <div v-for="(file, index) in fileList" :key="index" class="preview-item">
                  <el-image
                    :src="getImageUrl(file)"
                    fit="cover"
                    class="preview-image"
                  ></el-image>
                  <el-button 
                    type="text" 
                    class="preview-remove"
                    @click="removeFile(file)"
                  >
                    <el-icon><delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item class="form-item submit-item">
              <el-button 
                type="primary" 
                class="submit-btn" 
                @click="submitForm"
                :disabled="!isFormValid"
                :loading="isSubmitting"
              >
                提交反馈
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Delete } from '@element-plus/icons-vue'

/* global BMap */

const router = useRouter()
const formRef = ref()
const mapContainer = ref()
let map = null
let marker = null

// 表单数据
const form = reactive({
  title: '',
  noiseType: '',
  description: '',
  longitude: 114.3055,
  latitude: 30.5928,
  district: '',
  detailAddress: ''
})

// 地图选中的位置信息
const selectedLongitude = ref(114.3055)
const selectedLatitude = ref(30.5928)
const selectedAddress = ref('请在地图上选择位置')
const selectedDistrict = ref('')

// 坐标输入
const latitudeInput = ref('')
const longitudeInput = ref('')

// 文件上传
const fileList = ref([])

// 提交状态
const isSubmitting = ref(false)

// 获取图片URL
const getImageUrl = (file) => {
  if (file.url) {
    return file.url
  } else if (file.raw) {
    return URL.createObjectURL(file.raw)
  }
  return ''
}

// 移除文件
const removeFile = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
}

// 表单验证规则
const rules = {
  noiseType: [
    { required: true, message: '请选择噪音类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 1, max: 200, message: '详细描述长度应在 1-200 个字符之间', trigger: 'blur' }
  ]
}

// 计算表单是否有效
const isFormValid = computed(() => {
  return form.noiseType &&
         form.description &&
         selectedAddress.value !== '请在地图上选择位置'
})

// 监听表单变化，更新地图标记
watch(
  [selectedLongitude, selectedLatitude],
  ([newLng, newLat]) => {
    form.longitude = newLng
    form.latitude = newLat
    updateMarker()
  }
)

// 初始化百度地图
onMounted(() => {
  // 加载百度地图 API
  loadBaiduMapScript()
})

// 加载百度地图脚本
const loadBaiduMapScript = () => {
  const script = document.createElement('script')
  script.type = 'text/javascript'
  script.src = 'https://api.map.baidu.com/api?v=3.0&ak=E4805d16520de693a3fe707cdc962045&callback=initMap'
  script.async = true
  script.defer = true
  document.head.appendChild(script)
  
  // 全局回调函数
  window.initMap = initMap
}

// 地理编码并发控制
const geocodeQueue = []
const isProcessing = ref(false)

// 处理地理编码队列
const processGeocodeQueue = async () => {
  if (isProcessing.value || geocodeQueue.length === 0) return
  
  isProcessing.value = true
  
  try {
    const task = geocodeQueue.shift()
    await task()
  } catch (error) {
    console.error('地理编码错误:', error)
  } finally {
    isProcessing.value = false
    // 延迟处理下一个任务，避免请求过于频繁
    setTimeout(processGeocodeQueue, 1000)
  }
}

// 初始化地图
const initMap = () => {
  // 创建地图实例
  map = new BMap.Map(mapContainer.value)
  
  // 设置中心点和缩放级别
  const point = new BMap.Point(selectedLongitude.value, selectedLatitude.value)
  map.centerAndZoom(point, 11)
  
  // 添加控件
  map.addControl(new BMap.NavigationControl())
  map.addControl(new BMap.ScaleControl())
  map.addControl(new BMap.OverviewMapControl())
  
  // 启用滚轮缩放
  map.enableScrollWheelZoom(true)
  
  // 添加点击事件
  map.addEventListener('click', handleMapClick)
  
  // 添加初始标记
  updateMarker()
}

// 处理地图点击
const handleMapClick = (e) => {
  selectedLongitude.value = e.point.lng
  selectedLatitude.value = e.point.lat
  
  // 将地理编码任务加入队列
  geocodeQueue.push(() => {
    return new Promise((resolve) => {
      // 逆地理编码获取地址
      const geocoder = new BMap.Geocoder()
      geocoder.getLocation(e.point, (result) => {
        if (result) {
          selectedAddress.value = result.address
          selectedDistrict.value = result.addressComponents.district
          form.district = result.addressComponents.district
          form.detailAddress = result.address
        }
        resolve()
      })
    })
  })
  
  // 开始处理队列
  processGeocodeQueue()
}

// 更新地图标记
const updateMarker = () => {
  if (!map) return
  
  // 移除旧标记
  if (marker) {
    map.removeOverlay(marker)
  }
  
  // 添加新标记
  const point = new BMap.Point(selectedLongitude.value, selectedLatitude.value)
  marker = new BMap.Marker(point)
  map.addOverlay(marker)
  map.panTo(point)
}

// 处理文件选择
const handleFileChange = (file, files) => {
  // 检查文件数量
  if (files.length > 5) {
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
  
  // 更新文件列表（保持响应式）
  fileList.value = [...files]
  console.log('文件列表更新后:', fileList.value)
}

// 处理文件移除
const handleFileRemove = (file, files) => {
  fileList.value = [...files]
  console.log('文件列表更新后:', fileList.value)
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value || isSubmitting.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        isSubmitting.value = true
        
        // 构建FormData，一次性提交所有数据
        const formData = new FormData()
        formData.append('title', '噪音投诉')
        formData.append('description', form.description)
        formData.append('noiseType', form.noiseType)
        formData.append('longitude', form.longitude)
        formData.append('latitude', form.latitude)
        formData.append('district', form.district)
        formData.append('detailAddress', form.detailAddress)
        
        // 添加图片文件
        fileList.value.forEach(file => {
          formData.append('images', file.raw, file.name)
        })
        
        // 发送请求创建投诉（包含文件）
        const response = await fetch('/api/complaint', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
            // 注意：使用FormData时，不需要设置Content-Type
          },
          body: formData
        })
        
        const data = await response.json()
        
        if (data.code === 200) {
          ElMessage.success('投诉提交成功！')
          // 跳转到投诉列表页面
          router.push('/app/complaint/list')
        } else {
          ElMessage.error(data.message || '投诉提交失败')
        }
      } catch (error) {
        ElMessage.error('投诉提交失败：' + (error.response?.data?.message || '网络错误'))
      } finally {
        isSubmitting.value = false
      }
    }
  })
}



// 处理坐标输入
const handleCoordinateInput = () => {
  const lat = parseFloat(latitudeInput.value)
  const lng = parseFloat(longitudeInput.value)
  
  if (!isNaN(lat) && !isNaN(lng)) {
    selectedLatitude.value = lat
    selectedLongitude.value = lng
    
    // 将地理编码任务加入队列
    geocodeQueue.push(() => {
      return new Promise((resolve) => {
        const point = new BMap.Point(lng, lat)
        // 逆地理编码获取地址
        const geocoder = new BMap.Geocoder()
        geocoder.getLocation(point, (result) => {
          if (result) {
            selectedAddress.value = result.address
            selectedDistrict.value = result.addressComponents.district
            form.district = result.addressComponents.district
            form.detailAddress = result.address
          }
          resolve()
        })
      })
    })
    
    // 开始处理队列
    processGeocodeQueue()
  }
}

// 返回
const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
/* 统一容器框架 */
.page-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}



.content-wrapper {
  flex: 1;
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.page-title {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  text-align: center;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

/* 主容器 */
.main-container {
  display: flex;
  gap: 20px;
  min-height: 600px;
  align-items: stretch;
  justify-content: space-between;
}

/* 地图容器 */
.map-container-wrapper {
  flex: 1;
  min-width: 400px;
  max-width: 50%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 20px;
}

.map-container-wrapper:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

.map-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f2f5;
  background-color: #f8f9fa;
}

.map-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.map-container {
  width: 100%;
  flex: 1;
  min-height: 350px;
  max-height: 400px;
  border: none;
  border-radius: 0;
  overflow: hidden;
}

.map-footer {
  padding: 15px 20px;
  border-top: 1px solid #f0f2f5;
  background-color: #f8f9fa;
}

.coordinate-inputs {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.coordinate-inputs .el-input {
  flex: 1;
}

.address-display {
  margin: 15px 0;
  padding: 15px;
  background-color: #ffffff;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.address-display-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.address-icon {
  margin-right: 8px;
}

.address-content {
  font-size: 13px;
}

.address-line {
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
}

.address-line:last-child {
  margin-bottom: 0;
}

.address-line .address-label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  flex-shrink: 0;
  margin-right: 10px;
}

.address-line .address-value {
  color: #303133;
  flex: 1;
  word-break: break-all;
}

.map-tip {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.map-tip .tip-icon {
  margin-right: 5px;
}

/* 表单容器 */
.form-container {
  flex: 1;
  min-width: 400px;
  max-width: 50%;
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow-y: auto;
  min-height: 500px;
}

.form-container:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

.form-header {
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
}

.form-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 表单布局 */
.custom-form {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.form-control {
  width: 100%;
  font-size: 14px;
}

.el-select {
  width: 100%;
}

.el-select .el-input__inner {
  padding: 10px;
  font-size: 14px;
  width: 100%;
}

.el-textarea {
  width: 100%;
}

.el-textarea__inner {
  padding: 10px;
  font-size: 14px;
  border-radius: 4px;
  resize: vertical;
  width: 100%;
}

.textarea {
  min-height: 100px;
  resize: vertical;
  width: 100%;
}

/* 上传区域 */
.upload-area {
  width: 100%;
  box-sizing: border-box;
  text-align: center;
}

.upload-area .el-upload-dragger {
  border: 1px dashed #CCCCCC;
  border-radius: 4px;
  padding: 40px 20px;
  transition: all 0.3s ease;
  background-color: #ffffff;
}

.upload-area .el-upload-dragger:hover {
  border-color: #1890FF;
  background-color: #f0f9ff;
}

.upload-icon {
  font-size: 24px;
  color: #999999;
  margin-bottom: 16px;
}

.upload-text {
  color: #666666;
  font-size: 14px;
}

.upload-hint {
  font-size: 12px;
  color: #999999;
  margin-top: 8px;
  line-height: 1.4;
}

/* 上传预览 */
.upload-preview {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.preview-remove {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
}

.preview-remove:hover {
  background-color: rgba(0, 0, 0, 0.8);
  color: #ffffff;
}

/* 提交按钮 */
.submit-item {
  margin-top: 16px;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  padding: 12px;
  background-color: #1890FF;
  border-color: #1890FF;
  color: #ffffff;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.submit-btn:active {
  background-color: #096dd9;
  border-color: #096dd9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.el-button {
  transition: all 0.3s ease;
}

.el-button--primary:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

.el-button--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}



/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .main-container {
    flex-direction: column;
    min-height: auto;
  }
  
  .map-container-wrapper,
  .form-container {
    min-width: 100%;
    max-width: 100%;
    min-height: 500px;
    max-height: none;
  }
  
  .map-container {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 10px;
  }
  
  .page-title {
    font-size: 20px;
    margin-bottom: 20px;
  }
  
  .map-container-wrapper,
  .form-container {
    padding: 15px;
    min-height: 450px;
  }
  
  .map-container {
    min-height: 350px;
  }
  
  .address-line .address-label {
    width: 70px;
    font-size: 13px;
  }
  
  .address-line .address-value {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .map-container-wrapper,
  .form-container {
    min-width: 100%;
    padding: 12px;
  }
  
  .map-container {
    min-height: 300px;
  }
  
  .form-container {
    min-height: 400px;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
  
  .coordinate-inputs {
    flex-direction: column;
  }
  
  .address-line {
    flex-direction: column;
  }
  
  .address-line .address-label {
    margin-bottom: 4px;
  }
}

/* 加载动画 */
.loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 成功提示动画 */
.success-msg {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 滚动条样式 */
.form-container::-webkit-scrollbar {
  width: 6px;
}

.form-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.form-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.form-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>