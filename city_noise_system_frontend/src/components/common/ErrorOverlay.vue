<template>
  <div v-if="visible" class="error-overlay">
    <div class="error-content">
      <el-icon :size="24"><warning-filled /></el-icon>
      <p>{{ message }}</p>
      <el-button v-if="showRetry" @click="$emit('retry')">重新加载</el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  message: {
    type: String,
    default: '加载失败'
  },
  showRetry: {
    type: Boolean,
    default: true
  }
})

defineEmits(['retry'])
</script>

<style scoped>
.error-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.error-content {
  text-align: center;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.error-content p {
  margin: 16px 0;
  color: #606266;
}

</style>