<template>
  <div class="stat-card">
    <div class="stat-icon" :style="{ color: iconColor, backgroundColor: iconBgColor }">
      <el-icon :size="iconSize">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="stat-info">
      <div class="stat-header">
        <p class="stat-title">{{ title }}</p>
        <div v-if="trend" class="stat-trend" :class="trend">
          <el-icon :size="14">
            <component :is="trend === 'up' ? ArrowUp : ArrowDown" />
          </el-icon>
          <span>{{ trendValue }}%</span>
        </div>
      </div>
      <p class="stat-value">{{ formatValue(value) }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  icon: {
    type: String,
    required: true
  },
  iconColor: {
    type: String,
    default: 'var(--primary-color)'
  },
  iconSize: {
    type: Number,
    default: 24
  },
  trend: {
    type: String,
    default: '',
    validator: (value) => ['', 'up', 'down'].includes(value)
  },
  trendValue: {
    type: Number,
    default: 0
  }
})

// 计算图标背景色
const iconBgColor = computed(() => {
  return props.iconColor.replace('var(--', 'var(--').replace(')', '-light)')
})

// 格式化数值
const formatValue = (val) => {
  if (typeof val === 'number') {
    return val.toLocaleString()
  }
  return val
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg);
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
  border: 1px solid var(--border-secondary);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-md);
  font-size: var(--font-size-lg);
  transition: all var(--transition-fast);
}

.stat-card:hover .stat-icon {
  transform: scale(1.05);
}

.stat-info {
  flex: 1;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.stat-title {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  margin: 0;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  padding: 2px 8px;
  border-radius: var(--border-radius-full);
  transition: all var(--transition-fast);
}

.stat-trend.up {
  color: var(--success-color);
  background-color: var(--success-light);
}

.stat-trend.down {
  color: var(--danger-color);
  background-color: var(--danger-light);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0;
  transition: all var(--transition-fast);
}

.stat-card:hover .stat-value {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: var(--breakpoint-md)) {
  .stat-card {
    padding: var(--spacing-md);
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: var(--font-size-base);
  }
  
  .stat-value {
    font-size: var(--font-size-xl);
  }
  
  .stat-title {
    font-size: var(--font-size-xs);
  }
}
</style>