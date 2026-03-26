<template>
  <el-button
    :type="type"
    :size="size"
    :plain="plain"
    :round="round"
    :circle="circle"
    :disabled="disabled"
    :loading="loading"
    :icon="icon"
    :autofocus="autofocus"
    :native-type="nativeType"
    :class="[
      'custom-button',
      { 
        'custom-button--block': block,
        'custom-button--elevated': elevated
      }
    ]"
    @click="handleClick"
    @mousedown="$emit('mousedown', $event)"
    @mouseup="$emit('mouseup', $event)"
    @mouseenter="$emit('mouseenter', $event)"
    @mouseleave="$emit('mouseleave', $event)"
  >
    <template v-if="$slots.default">
      <slot></slot>
    </template>
    <template v-else>
      {{ label }}
    </template>
  </el-button>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  // 按钮类型：primary / success / warning / danger / info
  type: {
    type: String,
    default: 'primary'
  },
  // 按钮尺寸：large / default / small
  size: {
    type: String,
    default: 'default'
  },
  // 按钮文本
  label: {
    type: String,
    default: ''
  },
  // 是否为朴素按钮
  plain: {
    type: Boolean,
    default: false
  },
  // 是否为圆角按钮
  round: {
    type: Boolean,
    default: false
  },
  // 是否为圆形按钮
  circle: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },
  // 图标
  icon: {
    type: String,
    default: ''
  },
  // 是否为块级按钮
  block: {
    type: Boolean,
    default: false
  },
  // 是否为悬浮按钮
  elevated: {
    type: Boolean,
    default: false
  },
  // 原生type属性
  nativeType: {
    type: String,
    default: 'button'
  },
  // 是否自动聚焦
  autofocus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'click',
  'mousedown',
  'mouseup',
  'mouseenter',
  'mouseleave'
])

const handleClick = (event) => {
  emit('click', event)
}
</script>

<style scoped>
.custom-button {
  transition: all var(--transition-fast);
  border-radius: var(--border-radius-lg) !important;
  font-weight: var(--font-weight-medium) !important;
  position: relative;
  overflow: hidden;
}

.custom-button:hover:not(:disabled):not(.is-loading) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.custom-button:active:not(:disabled):not(.is-loading) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.custom-button:focus:not(:disabled):not(.is-loading) {
  box-shadow: 0 0 0 3px rgba(51, 102, 255, 0.1) !important;
}

/* 块级按钮 */
.custom-button--block {
  display: block;
  width: 100%;
}

/* 悬浮按钮 */
.custom-button--elevated {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.custom-button--elevated:hover:not(:disabled):not(.is-loading) {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 主要按钮样式 */
.custom-button.el-button--primary {
  background-color: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: white !important;
}

.custom-button.el-button--primary:hover:not(:disabled):not(.is-loading) {
  background-color: var(--primary-hover) !important;
  border-color: var(--primary-hover) !important;
}

/* 成功按钮样式 */
.custom-button.el-button--success {
  background-color: var(--success-color) !important;
  border-color: var(--success-color) !important;
  color: white !important;
}

.custom-button.el-button--success:hover:not(:disabled):not(.is-loading) {
  background-color: var(--success-hover) !important;
  border-color: var(--success-hover) !important;
}

/* 警告按钮样式 */
.custom-button.el-button--warning {
  background-color: var(--warning-color) !important;
  border-color: var(--warning-color) !important;
  color: white !important;
}

.custom-button.el-button--warning:hover:not(:disabled):not(.is-loading) {
  background-color: var(--warning-hover) !important;
  border-color: var(--warning-hover) !important;
}

/* 危险按钮样式 */
.custom-button.el-button--danger {
  background-color: var(--danger-color) !important;
  border-color: var(--danger-color) !important;
  color: white !important;
}

.custom-button.el-button--danger:hover:not(:disabled):not(.is-loading) {
  background-color: var(--danger-hover) !important;
  border-color: var(--danger-hover) !important;
}

/* 信息按钮样式 */
.custom-button.el-button--info {
  background-color: var(--info-color) !important;
  border-color: var(--info-color) !important;
  color: white !important;
}

.custom-button.el-button--info:hover:not(:disabled):not(.is-loading) {
  background-color: var(--info-hover) !important;
  border-color: var(--info-hover) !important;
}

/* 禁用状态 */
.custom-button:disabled {
  opacity: 0.6 !important;
  cursor: not-allowed !important;
  transform: none !important;
  box-shadow: none !important;
}

/* 加载状态 */
.custom-button.is-loading {
  transform: none !important;
  box-shadow: none !important;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .custom-button {
    border-radius: var(--border-radius-base) !important;
    padding: var(--spacing-sm) var(--spacing-md) !important;
    font-size: var(--font-size-sm) !important;
  }
  
  .custom-button:hover:not(:disabled):not(.is-loading) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
