<template>
  <div class="noise-map-container">
    <h2>附近噪音投诉点分布</h2>
    
    <!-- 超标预警广播栏 -->
    <div v-if="overStandardMonitors.length > 0" class="broadcast-bar">
      <el-alert
        :title="`⚠️ 超标预警：${overStandardMonitors.length}个监测点噪音超标`"
        type="warning"
        show-icon
        :closable="false"
      >
        <template #default>
          <div class="broadcast-content">
            <div 
              v-for="(monitor, index) in overStandardMonitors.slice(0, 3)" 
              :key="monitor.stationId" 
              class="broadcast-item"
              @click="locateOverStandardMonitor(monitor)"
              style="cursor: pointer;"
            >
              <span class="monitor-name">{{ monitor.stationName }}</span>
              <span class="noise-value">{{ monitor.noiseLevel }}dB</span>
              <span class="limit-value">(标准: {{ monitor.limitValue }}dB)</span>
            </div>
            <div v-if="overStandardMonitors.length > 3" class="broadcast-more">
              还有 {{ overStandardMonitors.length - 3 }} 个超标监测点...
            </div>
          </div>
        </template>
      </el-alert>
    </div>
    
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>地图查看</span>
        </div>
      </template>
      <div class="map-container" ref="mapContainer"></div>
      <div class="map-footer">
        <div class="search-area">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="噪音类型">
              <el-select v-model="searchForm.noiseType" placeholder="选择类型" @change="fetchNearbyComplaints">
                <el-option label="全部" value=""></el-option>
                <el-option label="施工噪音" value="CONSTRUCTION"></el-option>
                <el-option label="交通噪音" value="TRAFFIC"></el-option>
                <el-option label="生活噪音" value="LIVING"></el-option>
                <el-option label="商业噪音" value="COMMERCIAL"></el-option>
                <el-option label="工业噪音" value="INDUSTRIAL"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="显示类型">
              <el-checkbox-group v-model="displayOptions">
                <el-checkbox label="complaints">投诉点</el-checkbox>
                <el-checkbox label="monitors">监测点</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="地图类型">
              <el-select v-model="mapType" placeholder="选择地图类型" @change="changeMapType">
                <el-option label="默认地图" value="BMAP_NORMAL_MAP"></el-option>
                <el-option label="卫星地图" value="BMAP_SATELLITE_MAP"></el-option>
                <el-option label="混合地图" value="BMAP_HYBRID_MAP"></el-option>
                <el-option label="简洁地图" value="BMAP_PERSPECTIVE_MAP"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="refreshAllData">刷新数据</el-button>
              <el-button @click="locateMe">定位到我</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="map-info">
          <div class="info-item">
            <span class="info-label">当前位置：</span>
            <span class="info-value">{{ currentAddress || '定位中...' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">投诉点总数：</span>
            <span class="info-value">{{ nearbyComplaints.length }} 个</span>
          </div>
          <div class="info-item">
            <span class="info-label">监测点总数：</span>
            <span class="info-value">{{ monitorStations.length }} 个</span>
          </div>
          <div class="info-item" v-if="overStandardMonitors.length > 0">
            <span class="info-label">超标监测点：</span>
            <span class="info-value over-standard">{{ overStandardMonitors.length }} 个</span>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>投诉点列表</span>
        </div>
      </template>
      <el-table :data="nearbyComplaints" style="width: 100%" v-loading="loading">
        <el-table-column label="编号" width="80">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200"></el-table-column>
        <el-table-column prop="noiseType" label="噪音类型" width="120">
          <template #default="scope">
            {{ getNoiseTypeText(scope.row.noiseType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="distance" label="距离" width="100">
          <template #default="scope">
            {{ scope.row.distance }} 米
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row.id)">查看详情</el-button>
            <el-button size="small" type="primary" @click="viewLocation(scope.row)">查看定位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-card v-if="monitorStations.length > 0">
      <template #header>
        <div class="card-header">
          <span>监测点列表</span>
        </div>
      </template>
      <el-table :data="monitorStations" style="width: 100%" v-loading="monitorLoading">
        <el-table-column label="编号" width="80">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="stationName" label="监测点名称" width="200"></el-table-column>
        <el-table-column prop="address" label="地址" min-width="200"></el-table-column>
        <el-table-column prop="district" label="区域" width="120"></el-table-column>
        <el-table-column prop="functionalZone" label="功能区" width="120">
          <template #default="scope">
            {{ getFunctionalZoneText(scope.row.functionalZone) }}
          </template>
        </el-table-column>
        <el-table-column label="最新数据" width="150">
          <template #default="scope">
            <div v-if="scope.row.latestData && scope.row.latestData.noiseLevel">
              <div>{{ scope.row.latestData.noiseLevel }}dB</div>
              <div class="data-time">{{ formatTime(scope.row.latestData.monitorTime) }}</div>
            </div>
            <div v-else class="no-data">无数据</div>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewMonitorLocation(scope.row)">查看定位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const mapContainer = ref()
let map = null
let marker = null
let complaintMarkers = []
let monitorMarkers = []

const loading = ref(false)
const monitorLoading = ref(false)
const nearbyComplaints = ref([])
const monitorStations = ref([])
const overStandardMonitors = ref([])
const currentAddress = ref('')
const currentLocation = ref({ lng: 114.3055, lat: 30.5928 })
const displayOptions = ref(['complaints', 'monitors'])
const mapType = ref('BMAP_NORMAL_MAP')

const searchForm = ref({
  radius: '1000',
  noiseType: ''
})

// 环境噪声限值
const noiseLimits = {
  'CLASS_1': { day: 55, night: 45 },
  'CLASS_2': { day: 60, night: 50 },
  'CLASS_3': { day: 65, night: 55 },
  'CLASS_4A': { day: 70, night: 55 },
  'CLASS_4B': { day: 70, night: 60 }
}

// 数据缓存
const dataCache = ref({
  complaints: null,
  monitors: null,
  overStandard: null,
  lastUpdate: null
})

// 脉动动画计时器
let pulseAnimations = []

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

const getFunctionalZoneText = (zone) => {
  const zoneMap = {
    'CLASS_1': '一类区',
    'CLASS_2': '二类区',
    'CLASS_3': '三类区',
    'CLASS_4A': '四类区A',
    'CLASS_4B': '四类区B'
  }
  return zoneMap[zone] || zone
}

// 获取标准限值
const getStandardLimit = (zone, periodType) => {
  // 使用数据本身的periodType来判断是白天还是夜间
  const isDay = periodType === 'DAY'
  return isDay ? noiseLimits[zone]?.day || 60 : noiseLimits[zone]?.night || 50
}

const formatTime = (time) => {
  if (!time) return ''
  let formattedTime = time
  if (formattedTime.includes('T')) {
    formattedTime = formattedTime.replace('T', ' ')
  }
  return formattedTime
}

// 初始化百度地图
onMounted(() => {
  // 加载百度地图 API
  loadBaiduMapScript()
  
  // 监听显示选项变化
  watch(displayOptions, () => {
    updateMapMarkers()
  })
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

// 初始化地图
const initMap = () => {
  // 创建地图实例
  map = new BMap.Map(mapContainer.value)
  
  // 设置中心点和缩放级别
  const point = new BMap.Point(currentLocation.value.lng, currentLocation.value.lat)
  map.centerAndZoom(point, 15)
  
  // 添加控件
  map.addControl(new BMap.NavigationControl())
  map.addControl(new BMap.ScaleControl())
  map.addControl(new BMap.OverviewMapControl())
  
  // 启用滚轮缩放
  map.enableScrollWheelZoom(true)
  
  // 设置地图类型
  changeMapType()
  
  // 定位到当前位置
  locateMe()
  
  // 监听地图缩放事件，实现标记点聚合
  map.addEventListener('zoomend', () => {
    updateMapMarkers()
  })
}

// 切换地图类型
const changeMapType = () => {
  if (!map) return
  
  switch (mapType.value) {
    case 'BMAP_NORMAL_MAP':
      map.setMapType(BMAP_NORMAL_MAP)
      break
    case 'BMAP_SATELLITE_MAP':
      map.setMapType(BMAP_SATELLITE_MAP)
      break
    case 'BMAP_HYBRID_MAP':
      map.setMapType(BMAP_HYBRID_MAP)
      break
    case 'BMAP_PERSPECTIVE_MAP':
      // 检查是否支持简洁地图
      if (typeof BMAP_PERSPECTIVE_MAP !== 'undefined') {
        map.setMapType(BMAP_PERSPECTIVE_MAP)
      } else {
        ElMessage.warning('您的地图版本不支持简洁地图')
        mapType.value = 'BMAP_NORMAL_MAP'
        map.setMapType(BMAP_NORMAL_MAP)
      }
      break
    default:
      map.setMapType(BMAP_NORMAL_MAP)
  }
}

// 定位到当前位置
const locateMe = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude
        
        currentLocation.value = { lng, lat }
        
        // 更新地图中心
        const point = new BMap.Point(lng, lat)
        map.centerAndZoom(point, 15)
        
        // 添加当前位置标记
        if (marker) {
          map.removeOverlay(marker)
        }
        marker = new BMap.Marker(point)
        map.addOverlay(marker)
        marker.setTitle('我的位置')
        
        // 获取地址信息
        const geocoder = new BMap.Geocoder()
        geocoder.getLocation(point, (result) => {
          if (result) {
            currentAddress.value = result.address
          }
        })
        
        // 刷新所有数据
        refreshAllData()
      },
      (error) => {
        ElMessage.error('定位失败，请手动刷新地图')
        console.error('定位失败:', error)
        // 使用默认位置
        refreshAllData()
      }
    )
  } else {
    ElMessage.error('您的浏览器不支持地理定位')
    // 使用默认位置
    refreshAllData()
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  // 并行获取所有数据，不使用缓存
  await Promise.all([
    fetchNearbyComplaints(),
    fetchMonitorStations(),
    fetchOverStandardMonitors()
  ])
  
  // 更新地图标记
  updateMapMarkers()
  
  ElMessage.success('数据已刷新')
}

// 获取附近投诉点
const fetchNearbyComplaints = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/complaint/map-points`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      // 处理数据，计算距离
      const complaintsWithDistance = data.data.map(complaint => {
        // 计算距离
        const distance = calculateDistance(
          currentLocation.value.lat, 
          currentLocation.value.lng, 
          complaint.latitude, 
          complaint.longitude
        )
        // 格式化时间，移除 T 字符
        let formattedTime = complaint.createTime || new Date().toISOString();
        if (formattedTime.includes('T')) {
          formattedTime = formattedTime.replace('T', ' ');
        }
        return {
          ...complaint,
          distance: Math.round(distance),
          noiseType: complaint.noiseType || 'UNKNOWN',
          createTime: formattedTime
        }
      })
      
      // 按创建时间降序排序，最新的投诉排在前面
      const sortedComplaints = complaintsWithDistance.sort((a, b) => {
        return new Date(b.createTime) - new Date(a.createTime)
      })
      
      // 先存储所有投诉
      let allComplaints = sortedComplaints
      
      // 按噪音类型过滤
      if (searchForm.value.noiseType) {
        allComplaints = allComplaints.filter(
          complaint => complaint.noiseType === searchForm.value.noiseType
        )
      }
      
      // 现在将过滤后的投诉赋值给 nearbyComplaints
      nearbyComplaints.value = allComplaints
    } else {
      ElMessage.error(data.message || '获取附近投诉点失败')
    }
  } catch (error) {
    console.error('获取附近投诉点错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取监测点数据
const fetchMonitorStations = async () => {
  monitorLoading.value = true
  try {
    const response = await fetch(`/api/monitor/map-points`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      // 处理数据，计算距离
      const stationsWithDistance = data.data.map(station => {
        // 计算距离
        const distance = calculateDistance(
          currentLocation.value.lat, 
          currentLocation.value.lng, 
          station.latitude, 
          station.longitude
        )
        return {
          ...station,
          distance: Math.round(distance),
          latestData: station.noiseLevel ? {
            noiseLevel: station.noiseLevel,
            monitorTime: station.monitorTime,
            periodType: station.periodType
          } : null
        }
      })
      
      monitorStations.value = stationsWithDistance
    } else {
      ElMessage.error(data.message || '获取监测点数据失败')
    }
  } catch (error) {
    console.error('获取监测点错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    monitorLoading.value = false
  }
}

// 获取超标监测点
const fetchOverStandardMonitors = async () => {
  try {
    const response = await fetch(`/api/monitor/over-standard`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      overStandardMonitors.value = data.data
    } else {
      ElMessage.error(data.message || '获取超标监测点失败')
    }
  } catch (error) {
    console.error('获取超标监测点错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 角度转弧度
const toRadians = (degrees) => {
  return degrees * (Math.PI / 180);
};

// 计算两个经纬度之间的距离（单位：米）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371000; // 地球半径（米）
  const phi1 = toRadians(lat1);
  const phi2 = toRadians(lat2);
  const deltaPhi = toRadians(lat2 - lat1);
  const deltaLambda = toRadians(lon2 - lon1);

  const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) *
    Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

// 更新地图标记
const updateMapMarkers = () => {
  // 清除所有标记
  complaintMarkers.forEach(marker => {
    map.removeOverlay(marker)
  })
  complaintMarkers = []
  
  monitorMarkers.forEach(marker => {
    map.removeOverlay(marker)
  })
  monitorMarkers = []
  
  // 清除所有脉动动画
  pulseAnimations.forEach(animation => {
    clearInterval(animation)
  })
  pulseAnimations = []
  
  // 添加投诉点标记
  if (displayOptions.value.includes('complaints')) {
    updateComplaintMarkers()
  }
  
  // 添加监测点标记
  if (displayOptions.value.includes('monitors')) {
    updateMonitorMarkers()
  }
}

// 更新投诉点标记
const updateComplaintMarkers = () => {
  if (!map) return
  
  // 添加投诉点标记
  nearbyComplaints.value.forEach(complaint => {
    try {
      // 检查经纬度数据
      if (!complaint.longitude || !complaint.latitude) {
        console.warn('Complaint missing coordinates:', complaint)
        return
      }
      
      const point = new BMap.Point(complaint.longitude, complaint.latitude)
      
      // 创建默认红色图标（使用百度地图默认红色标记）
      const marker = new BMap.Marker(point)
      map.addOverlay(marker)
      
      // 添加信息窗口
      const noiseTypeText = getNoiseTypeText(complaint.noiseType);
      const statusText = getStatusText(complaint.status);
      const infoWindow = new BMap.InfoWindow(
        `<div style="width: 250px;">
          <h4 style="margin-top: 0;">${complaint.title}</h4>
          <p>噪音类型: ${noiseTypeText}</p>
          <p>处理状态: ${statusText}</p>
          <p>距离您: ${complaint.distance} 米</p>
          <p>提交时间: ${complaint.createTime}</p>
          <button onclick="viewComplaintDetail(${complaint.id})" style="margin-top: 10px; padding: 5px 10px; background-color: #409EFF; color: white; border: none; border-radius: 4px; cursor: pointer;">查看详情</button>
        </div>`
      )
      
      marker.addEventListener('click', () => {
        marker.openInfoWindow(infoWindow)
      })
      
      complaintMarkers.push(marker)
    } catch (error) {
      console.error('Error adding complaint marker:', error, complaint)
    }
  })
}

// 计算监测点所在区域的密度
const calculateDensity = (station, allStations) => {
  const radius = 500; // 500米半径，更精确地检测周围监测点
  let count = 0;
  
  allStations.forEach(otherStation => {
    if (station.id !== otherStation.id) {
      const distance = calculateDistance(
        station.latitude, 
        station.longitude, 
        otherStation.latitude, 
        otherStation.longitude
      );
      if (distance <= radius) {
        count++;
      }
    }
  });
  
  return count;
};

// 根据密度获取监测点大小
const getMarkerSizeByDensity = (density) => {
  // 监测点内圈固定为200米
  // 外圈雷达声波根据城区密度调整覆盖范围
  // 1000单位约等于1公里
  if (density >= 3) {
    // 城区/高密度区域：覆盖500m
    return 500; // 约覆盖500m
  } else {
    // 郊区/低密度区域：覆盖1公里
    return 1000; // 约覆盖1公里
  }
};

// 添加雷达声波效果
const addPulseAnimation = (point, baseRadius, isOverStandard = false) => {
  // 创建雷达声波效果的多个同心圆
  const circles = []
  const circleCount = 5 // 雷达波数量
  const maxRadius = baseRadius // 最大扩散半径（根据密度调整）
  const color = isOverStandard ? "red" : "blue"
  
  // 创建多个同心圆
  for (let i = 0; i < circleCount; i++) {
    const delay = i * 200 // 每个圆的延迟
    setTimeout(() => {
      const circle = new BMap.Circle(point, 0, {
        strokeColor: color,
        strokeWeight: 2,
        strokeOpacity: 0.8,
        fillColor: color,
        fillOpacity: 0.1
      })
      map.addOverlay(circle)
      monitorMarkers.push(circle)
      circles.push(circle)
      
      // 动画效果：向外扩散并逐渐消失
      let radius = 0
      const step = 30 // 扩散速度
      
      const animation = setInterval(() => {
        radius += step
        if (radius > maxRadius) {
          // 移除超出范围的圆并重新创建
          map.removeOverlay(circle)
          clearInterval(animation)
          
          // 重新创建新的圆，形成循环效果
          setTimeout(() => {
            const newCircle = new BMap.Circle(point, 0, {
              strokeColor: color,
              strokeWeight: 2,
              strokeOpacity: 0.8,
              fillColor: color,
              fillOpacity: 0.1
            })
            map.addOverlay(newCircle)
            monitorMarkers.push(newCircle)
            
            // 递归函数，用于创建并动画新的圆
            const createAndAnimateCircle = () => {
              const newCircle = new BMap.Circle(point, 0, {
                strokeColor: color,
                strokeWeight: 2,
                strokeOpacity: 0.8,
                fillColor: color,
                fillOpacity: 0.1
              })
              map.addOverlay(newCircle)
              monitorMarkers.push(newCircle)
              
              let newRadius = 0
              const newAnimation = setInterval(() => {
                newRadius += step
                if (newRadius > maxRadius) {
                  map.removeOverlay(newCircle)
                  clearInterval(newAnimation)
                  
                  // 动画结束后，再次创建新的圆，形成无限循环
                  setTimeout(createAndAnimateCircle, delay)
                } else {
                  const opacity = 0.8 - (newRadius / maxRadius) * 0.7
                  newCircle.setRadius(newRadius)
                  newCircle.setStrokeOpacity(opacity)
                  newCircle.setFillOpacity(opacity * 0.125)
                }
              }, 30)
              
              pulseAnimations.push(newAnimation)
            }
            
            // 开始创建并动画圆
            createAndAnimateCircle()
          }, delay)
        } else {
          const opacity = 0.8 - (radius / maxRadius) * 0.7
          circle.setRadius(radius)
          circle.setStrokeOpacity(opacity)
          circle.setFillOpacity(opacity * 0.125)
        }
      }, 30)
      
      pulseAnimations.push(animation)
    }, delay)
  }
  
  // 创建中心固定圆作为监测点标记（内圈固定为200米）
  const centerCircle = new BMap.Circle(point, 200, {
    strokeColor: color,
    strokeWeight: 3,
    strokeOpacity: 1,
    fillColor: color,
    fillOpacity: 0.3
  })
  map.addOverlay(centerCircle)
  monitorMarkers.push(centerCircle)
  
  // 返回中心圆对象，以便添加点击事件
  return centerCircle
}

// 更新监测点标记
const updateMonitorMarkers = () => {
  // 清除之前的脉动动画
  pulseAnimations.forEach(animation => {
    clearInterval(animation)
  })
  pulseAnimations = []
  
  // 添加监测点标记
  monitorStations.value.forEach(station => {
    const point = new BMap.Point(station.longitude, station.latitude)
    
    // 计算区域密度
    const density = calculateDensity(station, monitorStations.value)
    // 根据密度获取监测点大小
    const markerSize = getMarkerSizeByDensity(density)
    
    // 判断监测点是否超标
    const isOverStandard = station.latestData && station.latestData.noiseLevel > getStandardLimit(station.functionalZone, station.latestData.periodType)
    
    // 添加雷达声波效果，使用与标记对应的大小
    addPulseAnimation(point, markerSize, isOverStandard)
    
    // 添加信息窗口
    const functionalZoneText = getFunctionalZoneText(station.functionalZone);
    const infoWindow = new BMap.InfoWindow(
      `<div style="width: 250px;">
        <h4 style="margin-top: 0;">${station.stationName}</h4>
        <p>地址: ${station.address}</p>
        <p>功能区: ${functionalZoneText}</p>
        <p>区域: ${station.district}</p>
        <p>距离您: ${station.distance} 米</p>
        ${station.latestData && station.latestData.noiseLevel ? `<p>最新噪音: ${station.latestData.noiseLevel}dB</p>` : '<p>暂无数据</p>'}
        ${station.latestData ? `<p>监测时间: ${formatTime(station.latestData.monitorTime)}</p>` : ''}
      </div>`
    )
    
    // 创建一个可见的标记点作为监测点中心
    const marker = new BMap.Marker(point, {
      icon: new BMap.Icon('http://api.map.baidu.com/img/markers.png', new BMap.Size(20, 25), {
        offset: new BMap.Size(10, 25),
        imageOffset: new BMap.Size(0, 0 - 11 * 25) // 使用蓝色图标
      })
    })
    map.addOverlay(marker)
    monitorMarkers.push(marker)
    
    // 添加点击事件到标记
    marker.addEventListener('click', () => {
      map.openInfoWindow(infoWindow, point)
    })
    
    // 创建一个较大的透明覆盖物，用于捕获点击事件
    const clickArea = new BMap.Circle(point, 300, {
      strokeColor: "transparent",
      strokeWeight: 0,
      strokeOpacity: 0,
      fillColor: "transparent",
      fillOpacity: 0
    })
    map.addOverlay(clickArea)
    monitorMarkers.push(clickArea)
    
    // 添加点击事件到透明覆盖物
    clickArea.addEventListener('click', () => {
      map.openInfoWindow(infoWindow, point)
    })
  })
}

// 查看投诉详情
const viewDetail = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}

// 查看投诉点定位
const viewLocation = (complaint) => {
  if (!map) return
  
  // 创建投诉点位置
  const point = new BMap.Point(complaint.longitude, complaint.latitude)
  
  // 移动地图中心到投诉点
  map.centerAndZoom(point, 17)
  
  // 查找并高亮显示对应标记
  complaintMarkers.forEach(marker => {
    // 简单比较经纬度是否接近
    const markerPoint = marker.getPosition()
    if (Math.abs(markerPoint.lng - complaint.longitude) < 0.0001 && 
        Math.abs(markerPoint.lat - complaint.latitude) < 0.0001) {
      // 打开信息窗口
      const noiseTypeText = getNoiseTypeText(complaint.noiseType);
      const statusText = getStatusText(complaint.status);
      const infoWindow = new BMap.InfoWindow(
        `<div style="width: 250px;">
          <h4 style="margin-top: 0;">${complaint.title}</h4>
          <p>噪音类型: ${noiseTypeText}</p>
          <p>处理状态: ${statusText}</p>
          <p>距离您: ${complaint.distance} 米</p>
          <p>提交时间: ${complaint.createTime}</p>
          <button onclick="viewComplaintDetail(${complaint.id})" style="margin-top: 10px; padding: 5px 10px; background-color: #409EFF; color: white; border: none; border-radius: 4px; cursor: pointer;">查看详情</button>
        </div>`
      )
      marker.openInfoWindow(infoWindow)
    }
  })
  
  ElMessage.success(`已定位到投诉点: ${complaint.title}`)
}

// 查看监测点定位
const viewMonitorLocation = (station) => {
  if (!map) return
  
  // 创建监测点位置
  const point = new BMap.Point(station.longitude, station.latitude)
  
  // 移动地图中心到监测点
  map.centerAndZoom(point, 17)
  
  ElMessage.success(`已定位到监测点: ${station.stationName}`)
}

// 定位超标监测点
const locateOverStandardMonitor = (monitor) => {
  if (!map) return
  
  // 在监测点列表中查找对应的监测点
  const station = monitorStations.value.find(s => s.stationName === monitor.stationName)
  
  if (station) {
    // 调用viewMonitorLocation方法，确保与监测点列表的查看定位功能完全一致
    viewMonitorLocation(station)
  } else {
    // 如果找不到对应的监测点，使用超标预警数据中的经纬度
    const point = new BMap.Point(monitor.longitude, monitor.latitude)
    
    // 移动地图中心到监测点
    map.centerAndZoom(point, 17)
    
    // 创建并打开信息窗口
    const infoWindow = new BMap.InfoWindow(
      `<div style="width: 250px;">
        <h4 style="margin-top: 0;">${monitor.stationName}</h4>
        <p>地址: ${monitor.address || '未知'}</p>
        <p>最新噪音: ${monitor.noiseLevel}dB</p>
        <p>标准: ${monitor.limitValue}dB</p>
      </div>`
    )
    
    map.openInfoWindow(infoWindow, point)
    
    ElMessage.success(`已定位到超标监测点: ${monitor.stationName}`)
  }
}

// 全局方法，用于信息窗口中的链接
window.viewComplaintDetail = (id) => {
  router.push(`/app/complaint/detail/${id}`)
}
</script>

<style scoped>
.noise-map-container {
  padding: 20px;
  min-height: calc(100vh - var(--header-height));
}

/* 超标预警广播栏 */
.broadcast-bar {
  margin-bottom: 20px;
}

.broadcast-content {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
}

.broadcast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background-color: rgba(255, 229, 100, 0.2);
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

.monitor-name {
  font-weight: 500;
  color: #303133;
}

.noise-value {
  font-weight: bold;
  color: #f56c6c;
}

.limit-value {
  color: #606266;
  font-size: 12px;
}

.broadcast-more {
  color: #606266;
  font-size: 14px;
  margin-top: 5px;
}

.map-container {
  width: 100%;
  height: 500px;
  border-radius: 4px;
  overflow: hidden;
}

.map-footer {
  margin-top: 20px;
}

.search-form {
  margin-bottom: 15px;
}

.map-info {
  display: flex;
  gap: 30px;
  padding-top: 15px;
  border-top: 1px solid #f0f2f5;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  margin-right: 8px;
  color: #606266;
}

.info-value {
  color: #303133;
}

.info-value.over-standard {
  color: #f56c6c;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 监测点列表样式 */
.data-time {
  font-size: 12px;
  color: #909399;
}

.no-data {
  color: #909399;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .noise-map-container {
    padding: 10px;
  }
  
  .map-container {
    height: 400px;
  }
  
  .map-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-form {
    flex-wrap: wrap;
  }
  
  .broadcast-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .broadcast-item {
    width: 100%;
  }
}
</style>