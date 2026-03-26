<template>
  <div class="noise-quality-container">
    <h2>声环境质量实时数据</h2>
    
    <div class="card-grid">
      <!-- 地图卡片 -->
      <el-card class="map-card">
        <template #header>
          <div class="card-header">
            <span>声环境质量地图</span>
          </div>
        </template>
        <div class="map-container" ref="mapContainer"></div>
      </el-card>
      
      <!-- 数据表格卡片 -->
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>声环境质量实时数据({{ latestDataTime }})</span>
            <el-button type="primary" size="small" @click="dialogVisible = true">查看环境噪声限值</el-button>
          </div>
        </template>
        <div class="data-table">
          <el-table 
            :data="monitorData" 
            style="width: 100%" 
            v-loading="loading"
            @row-click="handleRowClick"
            row-key="id"
            :row-class-name="tableRowClassName"
          >
            <el-table-column prop="index" label="序号" width="80"></el-table-column>
            <el-table-column prop="district" label="辖区" width="100"></el-table-column>
            <el-table-column prop="stationName" label="点位名称" width="180"></el-table-column>
            <el-table-column prop="functionalZone" label="功能区类型" width="120">
              <template #default="scope">
                {{ getFunctionalZoneText(scope.row.functionalZone) }}
              </template>
            </el-table-column>
            <el-table-column prop="noiseLevel" label="等效声级" width="100">
              <template #default="scope">
                <span :class="{'over-standard': scope.row.noiseLevel > getStandardLimit(scope.row.functionalZone, scope.row.periodType)}">
                  {{ scope.row.noiseLevel || '暂无数据' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 环境噪声限值弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="环境噪声限值"
      width="90%"
      :close-on-click-modal="false"
      custom-class="noise-limits-dialog"
      :destroy-on-close="true"
    >
      <div class="noise-limits-content">
        <div class="limit-cards">
          <div 
            v-for="(standard, index) in noiseStandards" 
            :key="standard.id || index"
            class="limit-card"
          >
            <div class="card-header">
              <h3 class="zone-title">{{ getFunctionalZoneText(standard.functionalZone) }}</h3>
              <div class="limit-badges">
                <el-tag size="small" type="primary" effect="plain">
                  昼间: {{ standard.dayLimit }}dB
                </el-tag>
                <el-tag size="small" type="success" effect="plain">
                  夜间: {{ standard.nightLimit }}dB
                </el-tag>
              </div>
            </div>
            <div class="card-body">
              <p class="zone-description">{{ standard.description.replace(/^[\dab]+类区：/, '') }}</p>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" type="primary">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { monitorApi } from '@/services/api/monitor'

const mapContainer = ref()
let map = null
let monitorMarkers = []
let pulseAnimations = []

const loading = ref(false)
const monitorData = ref([])
const currentTime = ref('')
const latestDataTime = ref('')
const selectedStationId = ref(15) // 默认选中六合路32号
const dialogVisible = ref(false)
const noiseStandards = ref([])

// 环境噪声限值
const noiseLimits = {
  'CLASS_1': { day: 55, night: 45 },
  'CLASS_2': { day: 60, night: 50 },
  'CLASS_3': { day: 65, night: 55 },
  'CLASS_4A': { day: 70, night: 55 },
  'CLASS_4B': { day: 70, night: 60 }
}

// 初始化时间
const initCurrentTime = () => {
  updateCurrentTime()
  // 每分钟更新一次时间戳
  setInterval(updateCurrentTime, 60000)
}

// 更新当前时间
const updateCurrentTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}时`
}

// 获取功能区文本
const getFunctionalZoneText = (zone) => {
  const zoneMap = {
    'CLASS_1': '1类区',
    'CLASS_2': '2类区',
    'CLASS_3': '3类区',
    'CLASS_4A': '4a类区',
    'CLASS_4B': '4b类区'
  }
  return zoneMap[zone] || zone
}

// 获取标准限值
const getStandardLimit = (zone, periodType) => {
  // 使用数据本身的periodType来判断是白天还是夜间
  const isDay = periodType === 'DAY'
  return isDay ? noiseLimits[zone]?.day || 60 : noiseLimits[zone]?.night || 50
}

// 表格行样式
const tableRowClassName = ({ row }) => {
  if (row.noiseLevel > getStandardLimit(row.functionalZone, row.periodType)) {
    return 'over-standard-row'
  }
  return ''
}

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

// 武汉市14个行政区的GeoJSON数据（简化版）
const wuhanDistrictsGeoJSON = {
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": { "name": "江岸区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.283817, 30.624712],
          [114.304221, 30.615447],
          [114.316717, 30.604221],
          [114.322897, 30.595874],
          [114.318994, 30.586717],
          [114.302177, 30.583817],
          [114.289954, 30.588974],
          [114.283817, 30.602177],
          [114.283817, 30.624712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "江汉区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.283817, 30.583817],
          [114.302177, 30.583817],
          [114.309954, 30.579810],
          [114.304221, 30.574638],
          [114.290520, 30.574638],
          [114.283817, 30.579810],
          [114.283817, 30.583817]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "硚口区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.283817, 30.574638],
          [114.290520, 30.574638],
          [114.294423, 30.569465],
          [114.283817, 30.569465],
          [114.283817, 30.574638]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "汉阳区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.263817, 30.559465],
          [114.283817, 30.559465],
          [114.294423, 30.569465],
          [114.283817, 30.574638],
          [114.263817, 30.574638],
          [114.263817, 30.559465]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "武昌区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.302177, 30.559465],
          [114.322897, 30.559465],
          [114.334423, 30.569465],
          [114.322897, 30.579810],
          [114.302177, 30.583817],
          [114.302177, 30.559465]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "青山区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.334423, 30.595874],
          [114.354221, 30.595874],
          [114.366717, 30.604221],
          [114.354221, 30.615447],
          [114.334423, 30.615447],
          [114.334423, 30.595874]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "洪山区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.322897, 30.559465],
          [114.354221, 30.559465],
          [114.366717, 30.569465],
          [114.354221, 30.579810],
          [114.334423, 30.579810],
          [114.322897, 30.579810],
          [114.322897, 30.559465]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "东西湖区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.163817, 30.624712],
          [114.221770, 30.624712],
          [114.242217, 30.604221],
          [114.221770, 30.583817],
          [114.163817, 30.583817],
          [114.163817, 30.624712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "汉南区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.063817, 30.324712],
          [114.121770, 30.324712],
          [114.121770, 30.283817],
          [114.063817, 30.283817],
          [114.063817, 30.324712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "蔡甸区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.021770, 30.524712],
          [114.083817, 30.524712],
          [114.083817, 30.483817],
          [114.021770, 30.483817],
          [114.021770, 30.524712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "江夏区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.263817, 30.424712],
          [114.321770, 30.424712],
          [114.321770, 30.383817],
          [114.263817, 30.383817],
          [114.263817, 30.424712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "黄陂区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.321770, 30.724712],
          [114.383817, 30.724712],
          [114.383817, 30.683817],
          [114.321770, 30.683817],
          [114.321770, 30.724712]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "新洲区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.763817, 30.883817],
          [114.821770, 30.883817],
          [114.821770, 30.842177],
          [114.763817, 30.842177],
          [114.763817, 30.883817]
        ]]
      }
    },
    {
      "type": "Feature",
      "properties": { "name": "东湖新技术开发区" },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[
          [114.366717, 30.524712],
          [114.421770, 30.524712],
          [114.421770, 30.483817],
          [114.366717, 30.483817],
          [114.366717, 30.524712]
        ]]
      }
    }
  ]
};

// 初始化地图
const initMap = () => {
  // 创建地图实例
  map = new BMap.Map(mapContainer.value)
  
  // 设置中心点和缩放级别（武汉市中心）
  const point = new BMap.Point(114.305500, 30.592800) // 精确到小数点后6位
  map.centerAndZoom(point, 9) // 调整缩放级别，确保能显示所有区域（包括新洲区和黄陂区）
  
  // 添加控件
  map.addControl(new BMap.ScaleControl())
  
  // 启用滚轮缩放和拖拽，与噪音地图保持一致
  map.enableScrollWheelZoom()
  map.enableDragging()
  map.enableDoubleClickZoom()
  map.enablePinchToZoom()
  
  // 使用默认地图样式，与噪音地图保持一致
  
  // 添加地图类型控件
  map.addControl(new BMap.MapTypeControl({
    mapTypes: [
      BMAP_NORMAL_MAP,
      BMAP_SATELLITE_MAP,
      BMAP_HYBRID_MAP
    ]
  }))
  
  // 更新地图标记
  updateMapMarkers()
}



// 获取监测点数据
const fetchMonitorData = async () => {
  loading.value = true
  try {
    // 使用与噪音地图页面相同的接口，获取所有监测点数据
    const response = await monitorApi.getMonitorMapPoints()
    
    if (response.code === 200) {
      // 处理数据
      const stations = response.data
      const result = []
      let latestTime = ''
      
      for (let i = 0; i < stations.length; i++) {
        const station = stations[i]
        result.push({
          index: i + 1,
          id: station.id,
          stationName: station.stationName,
          district: station.district,
          functionalZone: station.functionalZone,
          noiseLevel: station.noiseLevel,
          monitorTime: station.monitorTime,
          longitude: station.longitude,
          latitude: station.latitude,
          address: station.address
        })
        
        // 更新最新数据时间
        if (station.monitorTime && station.monitorTime > latestTime) {
          latestTime = station.monitorTime
        }
      }
      
      monitorData.value = result
      
      // 更新最新数据时间
      if (latestTime) {
        let formattedTime = latestTime
        if (formattedTime.includes('T')) {
          formattedTime = formattedTime.replace('T', ' ')
        }
        latestDataTime.value = formattedTime
      } else {
        // 如果没有数据，使用当前时间
        latestDataTime.value = currentTime.value
      }
      
      // 更新地图标记
      updateMapMarkers()
    } else {
      ElMessage.error(response.message || '获取监测点数据失败')
    }
  } catch (error) {
    console.error('获取监测点数据错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    // 移除模拟数据功能，只依赖后端API
  } finally {
    loading.value = false
  }
}

// 已移除模拟数据功能，只依赖后端API

// 处理表格行点击
const handleRowClick = (row) => {
  selectedStationId.value = row.id
  
  // 重新渲染地图标记，将选中的监测点变为红色
  updateMapMarkers()
  
  // 定位到选中的监测点
  if (map && row.longitude && row.latitude) {
    const point = new BMap.Point(row.longitude, row.latitude)
    
    // 移动地图中心到监测点
    map.centerAndZoom(point, 17)
    
    // 创建并弹出信息窗口
    const functionalZoneText = getFunctionalZoneText(row.functionalZone);
    const infoWindow = new BMap.InfoWindow(
      `<div style="width: 250px;">
        <h4 style="margin-top: 0;">${row.stationName}</h4>
        <p>地址: ${row.address || '未知'}</p>
        <p>功能区: ${functionalZoneText}</p>
        <p>区域: ${row.district}</p>
        ${row.noiseLevel ? `<p>最新噪音: ${row.noiseLevel}dB</p>` : '<p>暂无数据</p>'}
        ${row.monitorTime ? `<p>监测时间: ${row.monitorTime}</p>` : ''}
      </div>`
    )
    
    map.openInfoWindow(infoWindow, point)
  }
}

// 更新地图标记
const updateMapMarkers = () => {
  if (!map) return
  
  // 清除现有监测点标记
  monitorMarkers.forEach(marker => {
    map.removeOverlay(marker)
  })
  monitorMarkers = []
  
  // 清除现有动画
  pulseAnimations.forEach(animation => {
    clearInterval(animation)
  })
  pulseAnimations = []
  
  // 添加监测点标记
  monitorData.value.forEach((station, index) => {
    const point = new BMap.Point(station.longitude, station.latitude)
    
    // 计算区域密度
    const density = calculateDensity(station, monitorData.value)
    // 根据密度获取监测点大小
    const markerSize = getMarkerSizeByDensity(density)
    
    // 判断监测点是否超标或被选中
    const isOverStandard = station.noiseLevel > getStandardLimit(station.functionalZone, station.periodType)
    const isSelected = station.id === selectedStationId.value
    
    // 添加雷达声波效果，使用超标状态或选中状态来决定颜色
    const centerCircle = addPulseAnimation(point, markerSize, isOverStandard || isSelected)
    
    // 添加点击事件
    centerCircle.addEventListener('click', () => {
      handleRowClick(station)
    })
  })
}

// 计算监测点所在区域的密度
const calculateDensity = (station, allStations) => {
  const radius = 500; // 500米半径，更精确地检测周围监测点
  let count = 0;
  
  allStations.forEach(otherStation => {
    if (station.id !== otherStation.id) {
      const distance = getDistance(
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

// 计算两个经纬度之间的距离（单位：米）
const getDistance = (lat1, lon1, lat2, lon2) => {
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
};

// 角度转弧度
const toRadians = (degrees) => {
  return degrees * (Math.PI / 180);
};

// 获取环境噪声限值数据
const fetchNoiseStandards = async () => {
  try {
    const response = await monitorApi.getNoiseStandards()
    
    if (response.code === 200) {
      noiseStandards.value = response.data
    } else {
      ElMessage.error(response.message || '获取环境噪声限值失败')
    }
  } catch (error) {
    console.error('获取环境噪声限值错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 添加监测点雷达声波效果
const addPulseAnimation = (point, baseRadius, isSelected = false) => {
  // 创建雷达声波效果的多个同心圆
  const circles = []
  const circleCount = 5 // 雷达波数量，与噪音地图一致
  const maxRadius = baseRadius // 最大扩散半径
  
  // 创建多个同心圆
  for (let i = 0; i < circleCount; i++) {
    const delay = i * 200 // 每个圆的延迟
    setTimeout(() => {
      const circle = new BMap.Circle(point, 0, {
        strokeColor: isSelected ? "red" : "blue", // 使用蓝色，与噪音地图一致
        strokeWeight: 2, // 线条宽度，与噪音地图一致
        strokeOpacity: 0.8, // 透明度，与噪音地图一致
        fillColor: isSelected ? "red" : "blue",
        fillOpacity: 0.1 // 填充透明度，与噪音地图一致
      })
      map.addOverlay(circle)
      monitorMarkers.push(circle)
      circles.push(circle)
      
      // 动画效果：向外扩散并逐渐消失
      let radius = 0
      const step = 30 // 扩散速度，与噪音地图一致
      
      const animation = setInterval(() => {
        radius += step
        if (radius > maxRadius) {
          // 移除超出范围的圆并重新创建
          map.removeOverlay(circle)
          clearInterval(animation)
          
          // 重新创建新的圆，形成循环效果
          setTimeout(() => {
            // 递归函数，用于创建并动画新的圆
            const createAndAnimateCircle = () => {
              const newCircle = new BMap.Circle(point, 0, {
                strokeColor: isSelected ? "red" : "blue",
                strokeWeight: 2,
                strokeOpacity: 0.8,
                fillColor: isSelected ? "red" : "blue",
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
              }, 30) // 动画速度，与噪音地图一致
              
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
      }, 30) // 动画速度，与噪音地图一致
      
      pulseAnimations.push(animation)
    }, delay)
  }
  
  // 创建中心固定圆作为监测点标记（内圈固定为200米，与噪音地图一致）
  const centerCircle = new BMap.Circle(point, 200, {
    strokeColor: isSelected ? "red" : "blue",
    strokeWeight: 3,
    strokeOpacity: 1,
    fillColor: isSelected ? "red" : "blue",
    fillOpacity: 0.3
  })
  map.addOverlay(centerCircle)
  monitorMarkers.push(centerCircle)
  
  // 返回中心圆对象，以便添加点击事件
  return centerCircle
}

// 初始化
onMounted(() => {
  initCurrentTime()
  loadBaiduMapScript()
  fetchMonitorData()
  fetchNoiseStandards()
})
</script>

<style scoped>
.noise-quality-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - var(--header-height));
}

.noise-quality-container h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 2px solid #409EFF;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(550px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.map-card,
.data-card {
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: none;
}

.map-card:hover,
.data-card:hover {
  box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-button {
  border-radius: 8px;
  font-weight: 500;
}

.map-container {
  width: 100%;
  height: 550px;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

.data-table {
  max-height: 500px;
  overflow-y: auto;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper th) {
  background-color: #f9f9f9;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__body-wrapper tr) {
  transition: all 0.2s ease;
}

:deep(.el-table__body-wrapper tr:hover) {
  background-color: #f5f7fa !important;
}

.over-standard {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

/* 确保整行红色效果覆盖Element Plus默认样式 */
:deep(.el-table__row.over-standard-row) {
  background-color: rgba(245, 108, 108, 0.15) !important;
}

:deep(.el-table__row.over-standard-row:hover) {
  background-color: rgba(245, 108, 108, 0.25) !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .map-container {
    height: 450px;
  }
  
  .noise-quality-container h2 {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .noise-quality-container {
    padding: 16px;
  }
  
  .card-grid {
    gap: 16px;
  }
  
  .map-container {
    height: 350px;
  }
  
  .noise-quality-container h2 {
    font-size: 18px;
    margin-bottom: 16px;
    padding-bottom: 12px;
  }
  
  .card-header span {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .noise-quality-container {
    padding: 12px;
  }
  
  .map-container {
    height: 300px;
  }
}

/* 数据加载动画 */
:deep(.el-loading-spinner) {
  margin-top: -20px;
}

:deep(.el-loading-text) {
  color: #409EFF;
  font-size: 14px;
}

/* 表格空状态 */
:deep(.el-table__empty-block) {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-table__empty-text) {
  color: #909399;
  font-size: 14px;
}

/* 噪声限值弹窗样式 */
.noise-limits-content {
  padding: 16px 0;
}

.limit-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.limit-card {
  border-radius: 12px;
  padding: 16px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.limit-card:hover {
  box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
  border-color: #dcdfe6;
}

/* 弹窗卡片头部样式 */
.limit-card .card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.zone-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #303133;
  text-align: center;
}

.limit-badges {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 8px 0;
  flex-wrap: wrap;
  justify-content: center;
}

.limit-badges .el-tag {
  font-size: 14px;
  padding: 6px 14px;
  border-radius: 18px;
  font-weight: 500;
}

.limit-badges .el-tag:first-child {
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409EFF;
}

.limit-badges .el-tag:last-child {
  background-color: #f0f9eb;
  border-color: #e1f5d9;
  color: #67C23A;
}

.card-body {
  flex: 1;
  display: flex;
  align-items: center;
}

.zone-description {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin: 0;
  text-align: center;
  word-break: break-word;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 弹窗自定义样式 */
:deep(.noise-limits-dialog) {
  z-index: 9999 !important;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.noise-limits-dialog .el-dialog__header) {
  padding: 20px 24px 16px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

:deep(.noise-limits-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  margin: 0 auto;
  color: #303133;
}

:deep(.noise-limits-dialog .el-dialog__body) {
  padding: 20px 24px;
  max-height: 70vh;
  overflow-y: auto;
  box-sizing: border-box;
}

:deep(.noise-limits-dialog .el-dialog__wrapper) {
  z-index: 9999 !important;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 100vw;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
}

:deep(.noise-limits-dialog .el-dialog) {
  margin: 0 !important;
  position: relative;
  top: auto;
  left: auto;
  transform: none;
  z-index: 10000 !important;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px 0 rgba(0, 0, 0, 0.2);
  min-width: 300px;
  max-width: 90vw;
  width: auto;
}

:deep(.noise-limits-dialog .el-dialog__footer) {
  padding: 16px 24px 20px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

/* 调整卡片容器宽度 */
.noise-limits-content {
  width: 100%;
  max-width: 100%;
}

.limit-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  width: 100%;
  max-width: 100%;
}

.limit-card {
  border-radius: 12px;
  padding: 16px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

/* 响应式设计 - 弹窗 */
@media (max-width: 1200px) {
  .limit-cards {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .limit-cards {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .limit-card {
    padding: 16px;
  }
  
  .limit-card .card-header {
    gap: 8px;
  }
  
  .zone-title {
    font-size: 16px;
  }
  
  .limit-badges {
    gap: 8px;
  }
  
  .limit-badges .el-tag {
    font-size: 12px;
    padding: 4px 12px;
  }
  
  .zone-description {
    font-size: 13px;
    text-align: left;
  }
  
  :deep(.noise-limits-dialog .el-dialog__body) {
    padding: 16px;
    max-height: 80vh;
  }
}

@media (max-width: 480px) {
  .limit-cards {
    grid-template-columns: 1fr;
  }
  
  .limit-card {
    padding: 12px;
  }
  
  .zone-title {
    font-size: 14px;
  }
  
  .limit-badges {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .limit-badges .el-tag {
    font-size: 11px;
    padding: 2px 8px;
  }
  
  .zone-description {
    font-size: 12px;
  }
  
  :deep(.noise-limits-dialog) {
    max-width: 98vw;
  }
}

</style>