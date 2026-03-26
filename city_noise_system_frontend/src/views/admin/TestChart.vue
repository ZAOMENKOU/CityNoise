<template>
  <div class="test-chart-container">
    <h2>图表测试</h2>
    <div ref="chartRef" style="width: 600px; height: 400px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chart = null

onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    
    const option = {
      title: {
        text: '测试图表',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: 10,
        left: 'center'
      },
      series: [
        {
          name: '测试数据',
          type: 'pie',
          radius: '60%',
          center: ['50%', '45%'],
          data: [
            { value: 1048, name: 'Search Engine' },
            { value: 735, name: 'Direct' },
            { value: 580, name: 'Email' },
            { value: 484, name: 'Union Ads' },
            { value: 300, name: 'Video Ads' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    
    chart.setOption(option)
  }
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
})
</script>

<style scoped>
.test-chart-container {
  padding: 20px;
}
</style>