<template>
  <div class="statistics-container">
    <h2>统计分析</h2>
    
    <!-- 数据类型切换按钮 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-button-group>
          <el-button 
            type="primary" 
            :plain="activeDataType !== 'complaint'"
            @click="switchDataType('complaint')"
          >
            投诉数据
          </el-button>
          <el-button 
            type="primary" 
            :plain="activeDataType !== 'monitor'"
            @click="switchDataType('monitor')"
          >
            监测数据
          </el-button>
        </el-button-group>
      </el-col>
    </el-row>
    
    <!-- 投诉数据统计 -->
    <template v-if="activeDataType === 'complaint'">
      <div class="overview-cards">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">总用户数</div>
            <div class="card-value">{{ statistics.totalUsers }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">总投诉数</div>
            <div class="card-value">{{ statistics.totalComplaints }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">待处理投诉</div>
            <div class="card-value">{{ statistics.pendingComplaints }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">处理中投诉</div>
            <div class="card-value">{{ statistics.processingComplaints }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">已解决投诉</div>
            <div class="card-value">{{ statistics.resolvedComplaints }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">解决率</div>
            <div class="card-value">{{ resolutionRate }}%</div>
          </div>
        </el-card>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="24" :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>按区域统计</span>
              </div>
            </template>
            <div class="chart-container" style="width: 100%; height: 400px;">
              <div ref="districtChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="24" :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>按类型统计</span>
              </div>
            </template>
            <div class="chart-container" style="width: 100%; height: 400px;">
              <div ref="typeChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 监测数据统计 -->
    <template v-else-if="activeDataType === 'monitor'">
      <div class="overview-cards">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">监测点总数</div>
            <div class="card-value">{{ monitorStatistics.totalStations }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">平均噪声值</div>
            <div class="card-value">{{ monitorStatistics.avgNoiseLevel }}dB</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">超标监测点</div>
            <div class="card-value">{{ monitorStatistics.overStandardStations }}</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">最高噪声值</div>
            <div class="card-value">{{ monitorStatistics.maxNoiseLevel }}dB</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">最低噪声值</div>
            <div class="card-value">{{ monitorStatistics.minNoiseLevel }}dB</div>
          </div>
        </el-card>
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-label">数据更新时间</div>
            <div class="card-value">{{ monitorStatistics.updateTime }}</div>
          </div>
        </el-card>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>按区域监测统计</span>
              </div>
            </template>
            <div class="chart-container">
              <div ref="monitorDistrictChartRef" class="chart" style="height: 400px;"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>按功能区监测统计</span>
              </div>
            </template>
            <div class="chart-container">
              <div ref="monitorZoneChartRef" class="chart" style="height: 400px;"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { adminApi } from '@/services/api/admin'
import { monitorApi } from '@/services/api/monitor'
import { commonApi } from '@/services/api/common'
import { complaintApi } from '@/services/api/complaint'

const loading = ref(false)
const activeDataType = ref('complaint') // 默认显示投诉数据

// 图表引用
const districtChartRef = ref(null)
const typeChartRef = ref(null)
const monitorDistrictChartRef = ref(null)
const monitorZoneChartRef = ref(null)

// 图表实例
let districtChart = null
let typeChart = null
let monitorDistrictChart = null
let monitorZoneChart = null

// 投诉数据
const statistics = ref({
  totalUsers: 0,
  totalComplaints: 0,
  pendingComplaints: 0,
  processingComplaints: 0,
  resolvedComplaints: 0
})

// 监测数据
const monitorStatistics = ref({
  totalStations: 0,
  avgNoiseLevel: 0,
  overStandardStations: 0,
  maxNoiseLevel: 0,
  minNoiseLevel: 0,
  updateTime: ''
})

// 统计数据
const districtStats = ref([])
const noiseTypeStats = ref([])

// 行政区数据
const districts = ref([])

const resolutionRate = computed(() => {
  if (statistics.value.totalComplaints === 0) {
    return 0
  }
  return Math.round((statistics.value.resolvedComplaints / statistics.value.totalComplaints) * 100)
})

// 切换数据类型
const switchDataType = async (type) => {
  activeDataType.value = type
  // 等待DOM渲染完成后初始化图表
  await nextTick()
  
  // 先销毁之前的图表实例，避免内存泄漏
  if (districtChart) {
    districtChart.dispose()
    districtChart = null
  }
  if (typeChart) {
    typeChart.dispose()
    typeChart = null
  }
  if (monitorDistrictChart) {
    monitorDistrictChart.dispose()
    monitorDistrictChart = null
  }
  if (monitorZoneChart) {
    monitorZoneChart.dispose()
    monitorZoneChart = null
  }
  
  if (type === 'complaint') {
    // 获取投诉统计数据
    await fetchStatistics()
    await fetchDistrictStats()
    await fetchNoiseTypeStats()
    // 确保数据已获取
    await nextTick()
    // 初始化投诉数据图表实例
    initComplaintCharts()
    // 确保图表已初始化
    await nextTick()
    updateComplaintCharts()
  } else if (type === 'monitor') {
    // 初始化监测数据图表实例
    initMonitorCharts()
    // 获取监测统计数据
    await fetchMonitorStatistics()
    // 确保图表已初始化
    await nextTick()
  }
}

// 获取行政区数据
const fetchDistricts = async () => {
  try {
    const response = await commonApi.getDistricts()
    if (response.code === 200) {
      districts.value = response.data
    } else {
      ElMessage.error(response.message || '获取行政区数据失败')
    }
  } catch (error) {
    console.error('获取行政区数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 获取投诉统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await adminApi.getStatistics()
    
    if (response.code === 200) {
      statistics.value = response.data
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取按区域统计数据
const fetchDistrictStats = async () => {
  try {
    const response = await complaintApi.getDistrictStats()
    if (response.code === 200) {
      // 将后端返回的Map转换为前端期望的数组格式
      const mapData = response.data
      districtStats.value = Object.entries(mapData).map(([district, count]) => ({
        district,
        count
      }))
      console.log('获取区域统计数据成功:', districtStats.value)
    } else {
      ElMessage.error(response.message || '获取区域统计数据失败')
      districtStats.value = []
      console.log('获取区域统计数据失败:', response.message)
    }
  } catch (error) {
    console.error('获取区域统计数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    districtStats.value = []
  }
}

// 获取按噪声类型统计数据
const fetchNoiseTypeStats = async () => {
  try {
    const response = await complaintApi.getNoiseTypeStats()
    if (response.code === 200) {
      // 将后端返回的Map转换为前端期望的数组格式
      const mapData = response.data
      noiseTypeStats.value = Object.entries(mapData).map(([noiseType, count]) => ({
        noiseType,
        count
      }))
    } else {
      ElMessage.error(response.message || '获取噪声类型统计数据失败')
      noiseTypeStats.value = []
    }
  } catch (error) {
    console.error('获取噪声类型统计数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    noiseTypeStats.value = []
  }
}

// 获取监测统计数据
const fetchMonitorStatistics = async () => {
  loading.value = true
  try {
    // 使用后端现有的map-points接口获取监测点数据
    const mapPointsResponse = await monitorApi.getMonitorMapPoints()
    // 使用与超标预警页面相同的接口获取超标监测点
    const overStandardResponse = await monitorApi.getOverStandardMonitors()
    
    if (mapPointsResponse.code === 200 && overStandardResponse.code === 200) {
      const stations = mapPointsResponse.data
      const overStandardStations = overStandardResponse.data
      
      // 计算统计数据
      const totalStations = stations.length
      let sumNoiseLevel = 0
      let overStandardCount = overStandardStations.length // 使用后端返回的超标监测点数量
      let maxNoiseLevel = 0
      let minNoiseLevel = Infinity
      let latestTime = ''
      
      stations.forEach(station => {
        if (station.noiseLevel) {
          sumNoiseLevel += station.noiseLevel
          
          // 更新最高和最低噪声值
          if (station.noiseLevel > maxNoiseLevel) {
            maxNoiseLevel = station.noiseLevel
          }
          if (station.noiseLevel < minNoiseLevel) {
            minNoiseLevel = station.noiseLevel
          }
        }
        
        // 更新最新数据时间
        if (station.monitorTime && station.monitorTime > latestTime) {
          latestTime = station.monitorTime
        }
      })
      
      // 计算平均噪声值
      const avgNoiseLevel = totalStations > 0 ? sumNoiseLevel / totalStations : 0
      
      // 格式化最新数据时间
      let formattedTime = latestTime
      if (formattedTime && formattedTime.includes('T')) {
        formattedTime = formattedTime.replace('T', ' ')
      }
      
      // 更新监测统计数据
      monitorStatistics.value = {
        totalStations,
        avgNoiseLevel: Math.round(avgNoiseLevel * 10) / 10,
        overStandardStations: overStandardCount,
        maxNoiseLevel,
        minNoiseLevel: minNoiseLevel === Infinity ? 0 : minNoiseLevel,
        updateTime: formattedTime || new Date().toLocaleString()
      }
      
      // 更新监测数据图表
      updateMonitorCharts(stations)
    } else {
      ElMessage.error(mapPointsResponse.message || overStandardResponse.message || '获取监测数据失败')
    }
  } catch (error) {
    console.error('获取监测数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化投诉数据图表
const initComplaintCharts = () => {
  if (districtChartRef.value && !districtChart) {
    districtChart = echarts.init(districtChartRef.value)
  }
  if (typeChartRef.value && !typeChart) {
    typeChart = echarts.init(typeChartRef.value)
  }
  // 初始化时就应用颜色配置
  if (districtChart) {
    districtChart.setOption({
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
    })
  }
  if (typeChart) {
    typeChart.setOption({
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
    })
  }
  // 不在这里调用updateComplaintCharts，避免重复更新
}

// 更新投诉数据图表
const updateComplaintCharts = () => {
  console.log('开始更新投诉数据图表');
  console.log('districtStats:', districtStats.value);
  console.log('noiseTypeStats:', noiseTypeStats.value);
  
  // 使用从后端API获取的真实数据
  let districtData = []
  let typeData = []
  
  // 处理区域统计数据
  if (districtStats.value && districtStats.value.length > 0) {
    districtData = districtStats.value.map(item => ({
      name: item.district || item.name || '未知区域',
      value: item.count || item.value || 0
    })).filter(item => item.value > 0)
    console.log('处理后的区域数据:', districtData);
  } else {
    console.log('没有区域统计数据');
  }
  
  // 确保颜色配置正确应用
  console.log('区域统计图表实例:', districtChart);
  console.log('区域统计图表数据:', districtData);
  
  // 处理噪声类型统计数据
  if (noiseTypeStats.value && noiseTypeStats.value.length > 0) {
    typeData = noiseTypeStats.value.map(item => {
      // 转换噪声类型编码为可读名称
      let typeName = item.noiseType || item.name || '未知类型'
      const typeMap = {
        'TRAFFIC': '交通噪声',
        'CONSTRUCTION': '施工噪声',
        'INDUSTRIAL': '工业噪声',
        'SOCIAL': '社会生活噪声',
        'LIVING': '生活噪声',
        'COMMERCIAL': '商业噪声'
      }
      if (typeMap[typeName]) {
        typeName = typeMap[typeName]
      }
      return {
        name: typeName,
        value: item.count || item.value || 0
      }
    }).filter(item => item.value > 0)
    console.log('处理后的噪声类型数据:', typeData);
  } else {
    console.log('没有噪声类型统计数据');
  }
  
  // 区域统计图表配置
  if (districtChart) {
    console.log('更新区域统计图表，数据:', districtData);
    console.log('区域统计图表实例:', districtChart);
    if (districtData.length === 0) {
      // 无数据时显示友好提示
      districtChart.setOption({
        title: {
          text: '按区域投诉统计',
          left: 'center'
        },
        tooltip: {},
        series: [
          {
            type: 'pie',
            radius: '60%',
            center: ['50%', '45%'],
            data: [{ name: '暂无数据', value: 1 }],
            itemStyle: {
              color: '#f0f0f0'
            },
            label: {
              show: true,
              formatter: '{b}',
              color: '#999'
            }
          }
        ]
      })
    } else {
      // 先设置颜色配置
      districtChart.setOption({
        color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
      })
      // 然后设置完整配置
      districtChart.setOption({
        title: {
          text: '按区域投诉统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          data: districtData.map(item => item.name)
        },
        series: [
          {
            name: '投诉数量',
            type: 'pie',
            radius: '60%',
            center: ['50%', '45%'],
            data: districtData,
            itemStyle: {
              borderRadius: 5,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}'
            }
          }
        ]
      })
    }
    console.log('区域统计图表更新完成');
  }
  
  // 类型统计图表配置
  if (typeChart) {
    console.log('更新类型统计图表，数据:', typeData);
    if (typeData.length === 0) {
      // 无数据时显示友好提示
      typeChart.setOption({
        title: {
          text: '按类型投诉统计',
          left: 'center'
        },
        tooltip: {},
        series: [
          {
            type: 'pie',
            radius: '60%',
            center: ['50%', '45%'],
            data: [{ name: '暂无数据', value: 1 }],
            itemStyle: {
              color: '#f0f0f0'
            },
            label: {
              show: true,
              formatter: '{b}',
              color: '#999'
            }
          }
        ]
      })
    } else {
      typeChart.setOption({
        title: {
          text: '按类型投诉统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          data: typeData.map(item => item.name)
        },
        color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'],
        series: [
          {
            name: '投诉数量',
            type: 'pie',
            radius: '60%',
            center: ['50%', '45%'],
            data: typeData,
            itemStyle: {
              borderRadius: 5,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}'
            }
          }
        ]
      })
    }
    console.log('类型统计图表更新完成');
  }
}

// 初始化监测数据图表
const initMonitorCharts = () => {
  if (monitorDistrictChartRef.value && !monitorDistrictChart) {
    monitorDistrictChart = echarts.init(monitorDistrictChartRef.value)
  }
  if (monitorZoneChartRef.value && !monitorZoneChart) {
    monitorZoneChart = echarts.init(monitorZoneChartRef.value)
  }
  // 只初始化图表实例，不获取数据
}

// 更新监测数据图表
const updateMonitorCharts = (stations = []) => {
  // 按区域统计数据
  const districtMap = new Map()
  stations.forEach(station => {
    const district = station.district
    if (district) {
      if (!districtMap.has(district)) {
        districtMap.set(district, {
          name: district,
          totalNoise: 0,
          count: 0,
          stations: []
        })
      }
      const districtData = districtMap.get(district)
      districtData.count++
      if (station.noiseLevel) {
        districtData.totalNoise += station.noiseLevel
        districtData.stations.push(station)
      }
    }
  })
  
  // 计算每个区域的平均噪声值
  let monitorDistrictData = Array.from(districtMap.values()).map(item => ({
    name: item.name,
    avgNoise: item.count > 0 ? parseFloat((item.totalNoise / item.count).toFixed(2)) : 0,
    count: item.count
  }))
  
  // 按功能区统计数据
  const zoneMap = new Map()
  const zoneNameMap = {
    'CLASS_1': '1类区',
    'CLASS_2': '2类区',
    'CLASS_3': '3类区',
    'CLASS_4A': '4a类区',
    'CLASS_4B': '4b类区'
  }
  
  stations.forEach(station => {
    const zone = station.functionalZone
    if (zone) {
      const zoneName = zoneNameMap[zone] || zone
      if (!zoneMap.has(zoneName)) {
        zoneMap.set(zoneName, {
          name: zoneName,
          totalNoise: 0,
          count: 0,
          stations: []
        })
      }
      const zoneData = zoneMap.get(zoneName)
      zoneData.count++
      if (station.noiseLevel) {
        zoneData.totalNoise += station.noiseLevel
        zoneData.stations.push(station)
      }
    }
  })
  
  // 计算每个功能区的平均噪声值
  const monitorZoneData = Array.from(zoneMap.values()).map(item => ({
    name: item.name,
    avgNoise: item.count > 0 ? parseFloat((item.totalNoise / item.count).toFixed(2)) : 0,
    count: item.count
  }))
  
  // 区域监测统计图表配置
  if (monitorDistrictChart) {
    if (monitorDistrictData.length === 0) {
      // 无数据时显示友好提示
      monitorDistrictChart.setOption({
        title: {
          text: '按区域监测统计',
          left: 'center'
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: ['暂无数据']
        },
        yAxis: [
          {
            type: 'value',
            name: '平均噪声值 (dB)',
            position: 'left'
          },
          {
            type: 'value',
            name: '监测点数量',
            position: 'right'
          }
        ],
        series: [
          {
            name: '平均噪声值',
            type: 'bar',
            data: [0]
          },
          {
            name: '监测点数量',
            type: 'line',
            yAxisIndex: 1,
            data: [0]
          }
        ]
      })
    } else {
      monitorDistrictChart.setOption({
        title: {
          text: '按区域监测统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['平均噪声值', '监测点数量'],
          bottom: 0
        },
        xAxis: {
          type: 'category',
          data: monitorDistrictData.map(item => item.name),
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '平均噪声值 (dB)',
            position: 'left'
          },
          {
            type: 'value',
            name: '监测点数量',
            position: 'right'
          }
        ],
        series: [
          {
            name: '平均噪声值',
            type: 'bar',
            data: monitorDistrictData.map(item => item.avgNoise)
          },
          {
            name: '监测点数量',
            type: 'line',
            yAxisIndex: 1,
            data: monitorDistrictData.map(item => item.count)
          }
        ]
      })
    }
  }
  
  // 功能区监测统计图表配置
  if (monitorZoneChart) {
    if (monitorZoneData.length === 0) {
      // 无数据时显示友好提示
      monitorZoneChart.setOption({
        title: {
          text: '按功能区监测统计',
          left: 'center'
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: ['暂无数据']
        },
        yAxis: [
          {
            type: 'value',
            name: '平均噪声值 (dB)',
            position: 'left'
          },
          {
            type: 'value',
            name: '监测点数量',
            position: 'right'
          }
        ],
        series: [
          {
            name: '平均噪声值',
            type: 'bar',
            data: [0]
          },
          {
            name: '监测点数量',
            type: 'line',
            yAxisIndex: 1,
            data: [0]
          }
        ]
      })
    } else {
      monitorZoneChart.setOption({
        title: {
          text: '按功能区监测统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['平均噪声值', '监测点数量'],
          bottom: 0
        },
        xAxis: {
          type: 'category',
          data: monitorZoneData.map(item => item.name)
        },
        yAxis: [
          {
            type: 'value',
            name: '平均噪声值 (dB)',
            position: 'left'
          },
          {
            type: 'value',
            name: '监测点数量',
            position: 'right'
          }
        ],
        series: [
          {
            name: '平均噪声值',
            type: 'bar',
            data: monitorZoneData.map(item => item.avgNoise)
          },
          {
            name: '监测点数量',
            type: 'line',
            yAxisIndex: 1,
            data: monitorZoneData.map(item => item.count)
          }
        ]
      })
    }
  }
}

// 监听数据类型切换
watch(activeDataType, (newType) => {
  // 图表初始化已经在switchDataType函数中处理，这里不再重复初始化
  // 只在需要时更新图表尺寸
  setTimeout(() => {
    handleResize()
  }, 100)
})

// 监听窗口大小变化，调整图表尺寸
const handleResize = () => {
  if (districtChart) districtChart.resize()
  if (typeChart) typeChart.resize()
  if (monitorDistrictChart) monitorDistrictChart.resize()
  if (monitorZoneChart) monitorZoneChart.resize()
}

onMounted(async () => {
  console.log('开始执行onMounted');
  // 等待DOM渲染完成
  await nextTick()
  console.log('DOM渲染完成');
  
  // 首先获取数据
  console.log('开始获取数据');
  // 首先获取行政区数据
  await fetchDistricts()
  // 然后获取统计数据
  await fetchStatistics()
  // 获取新的统计数据
  await fetchDistrictStats()
  await fetchNoiseTypeStats()
  
  // 数据获取完成后初始化图表
  console.log('数据获取完成，开始初始化图表');
  initComplaintCharts()
  
  // 确保图表已初始化
  await nextTick()
  // 更新图表
  updateComplaintCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  console.log('onMounted执行完成');
})

// 组件卸载时销毁图表实例
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (districtChart) districtChart.dispose()
  if (typeChart) typeChart.dispose()
  if (monitorDistrictChart) monitorDistrictChart.dispose()
  if (monitorZoneChart) monitorZoneChart.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  color: #409eff;
  margin: 10px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 概览卡片样式 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.overview-card {
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.card-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-value {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
}
</style>