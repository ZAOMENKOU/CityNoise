<template>
  <div class="worker-history-container">
    <h2>处理历史</h2>
    
    <el-card class="filter-card">
      <div class="filter-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="处理状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 120px;">
              <el-option label="全部" value=""></el-option>
              <el-option label="已处理" value="PROCESSED"></el-option>
              <el-option label="已完成" value="COMPLETED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="处理时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 300px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchHistoryData">查询</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card class="data-card">
      <div class="table-container">
        <el-table 
          :data="historyData" 
          style="width: 100%"
          v-loading="loading"
          border
        >
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="complaintTitle" label="投诉标题" min-width="200">
            <template #default="scope">
              <el-button type="text" @click="viewDetail(scope.row.complaintId)">{{ scope.row.complaintTitle }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="district" label="辖区" width="120"></el-table-column>
          <el-table-column prop="noiseType" label="噪音类型" width="120">
            <template #default="scope">
              {{ getNoiseTypeText(scope.row.noiseType) }}
            </template>
          </el-table-column>
          <el-table-column prop="processTime" label="处理时间" width="180"></el-table-column>
          <el-table-column prop="status" label="处理状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'primary'">
                {{ scope.row.status === 'COMPLETED' ? '已完成' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="处理结果" min-width="150"></el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  status: '',
  dateRange: []
})

// 处理历史数据
const historyData = ref([])

// 加载状态
const loading = ref(false)

// 分页信息
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 获取处理历史数据
const fetchHistoryData = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/worker/history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        status: searchForm.value.status,
        startDate: searchForm.value.dateRange[0] || '',
        endDate: searchForm.value.dateRange[1] || '',
        page: pagination.value.current,
        pageSize: pagination.value.pageSize
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      historyData.value = data.data.records || []
      pagination.value.total = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取处理历史失败')
    }
  } catch (error) {
    console.error('获取处理历史错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}



// 重置表单
const resetForm = () => {
  searchForm.value = {
    status: '',
    dateRange: []
  }
  pagination.value.current = 1
  fetchHistoryData()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  fetchHistoryData()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.value.current = current
  fetchHistoryData()
}

// 查看投诉详情
const viewDetail = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}

// 获取噪音类型文本
const getNoiseTypeText = (type) => {
  const typeMap = {
    'CONSTRUCTION': '施工噪音',
    'TRAFFIC': '交通噪音',
    'LIVING': '生活噪音',
    'COMMERCIAL': '商业噪音',
    'INDUSTRIAL': '工业噪音'
  }
  return typeMap[type] || type
}

// 初始化
onMounted(() => {
  fetchHistoryData()
})
</script>

<style scoped>
.worker-history-container {
  padding: 20px;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.filter-card {
  flex-shrink: 0;
}

.filter-form {
  margin-bottom: 0;
}

.data-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  padding: 15px 10px;
  border-top: 1px solid #ebeef5;
  background-color: #ffffff;
  width: 100%;
  box-sizing: border-box;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .pagination-container {
    padding: 10px;
  }
  
  .pagination-container .el-pagination {
    font-size: 12px;
    width: 100%;
  }
  
  .pagination-container .el-pagination__sizes {
    display: none;
  }
  
  .pagination-container .el-pagination__jump {
    display: none;
  }
  
  .pagination-container .el-pagination__total {
    display: none;
  }
  
  .pagination-container .el-pagination__rightwrapper {
    display: none;
  }
  
  .pagination-container .el-pagination__leftwrapper {
    width: 100%;
    display: flex;
    justify-content: center;
  }
  
  .pagination-container .el-pager {
    margin: 0;
  }
  
  .pagination-container .el-pager li {
    margin: 0 2px;
    min-width: 24px;
    height: 24px;
    line-height: 24px;
  }
  
  .pagination-container .el-pagination__prev,
  .pagination-container .el-pagination__next {
    min-width: 24px;
    height: 24px;
    line-height: 24px;
    margin: 0 2px;
  }
}

/* 表格样式优化 */
el-table {
  width: 100%;
  border-collapse: collapse;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .worker-history-container {
    padding: 15px;
    height: calc(100vh - 100px);
  }
}

@media (max-width: 768px) {
  .worker-history-container {
    padding: 10px;
    height: calc(100vh - 80px);
  }
  
  .filter-form {
    overflow-x: auto;
  }
  
  .demo-form-inline {
    min-width: 600px;
  }
  
  .table-container {
    margin-bottom: 10px;
  }
  
  el-table {
    font-size: 12px;
  }
  
  el-table-column {
    min-width: 80px;
  }
}

/* 小屏幕设备 */
@media (max-width: 480px) {
  .worker-history-container {
    padding: 8px;
  }
  
  h2 {
    font-size: 16px;
    margin-bottom: 10px;
  }
  
  .filter-card {
    margin-bottom: 10px;
  }
  
  .demo-form-inline {
    min-width: 400px;
  }
}
</style>