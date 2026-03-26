<template>
  <el-tag :type="tagType">{{ displayText }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': '',
    'ASSIGNED': 'info',
    'PROCESSING': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'danger',
    'CANCELLED': 'danger',
    '待处理': '',
    '处理中': 'warning',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || ''
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

const tagType = computed(() => getStatusType(props.status))
const displayText = computed(() => getStatusText(props.status))
</script>