<template>
  <div class="input-container" :class="{ 'input-container-error': error }">
    <label v-if="label" class="input-label">{{ label }}</label>
    <el-input
      v-model="modelValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :size="size"
      :prefix-icon="prefixIcon"
      :suffix-icon="suffixIcon"
      :clearable="clearable"
      :show-password="showPassword"
      :validate-event="validateEvent"
      class="custom-input"
      @input="$emit('update:modelValue', $event)"
      @focus="$emit('focus', $event)"
      @blur="$emit('blur', $event)"
      @change="$emit('change', $event)"
      @clear="$emit('clear', $event)"
    >
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>
      <template v-if="$slots.suffix" #suffix>
        <slot name="suffix"></slot>
      </template>
      <template v-if="$slots.prepend" #prepend>
        <slot name="prepend"></slot>
      </template>
      <template v-if="$slots.append" #append>
        <slot name="append"></slot>
      </template>
    </el-input>
    <div v-if="error" class="input-error">{{ error }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  prefixIcon: {
    type: String,
    default: ''
  },
  suffixIcon: {
    type: String,
    default: ''
  },
  clearable: {
    type: Boolean,
    default: false
  },
  showPassword: {
    type: Boolean,
    default: false
  },
  validateEvent: {
    type: Boolean,
    default: true
  },
  error: {
    type: String,
    default: ''
  }
})

defineEmits([
  'update:modelValue',
  'focus',
  'blur',
  'change',
  'clear'
])
</script>

<style scoped>
.input-container {
  margin-bottom: var(--spacing-md);
  transition: all var(--transition-fast);
}

.input-label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.input-container:hover .input-label {
  color: var(--primary-color);
}

.custom-input {
  width: 100%;
  border-radius: var(--border-radius-lg) !important;
  transition: all var(--transition-fast) !important;
  border: 1px solid var(--border-primary) !important;
}

.custom-input:hover {
  border-color: var(--primary-hover) !important;
  box-shadow: 0 0 0 3px rgba(51, 102, 255, 0.05) !important;
}

.custom-input:focus {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px rgba(51, 102, 255, 0.1) !important;
}

.custom-input:disabled {
  background-color: var(--bg-light) !important;
  border-color: var(--border-tertiary) !important;
  color: var(--text-quaternary) !important;
}

.input-container-error .custom-input {
  border-color: var(--danger-color) !important;
}

.input-container-error .custom-input:focus {
  box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.1) !important;
}

.input-error {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--danger-color);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .input-container {
    margin-bottom: var(--spacing-sm);
  }
  
  .input-label {
    font-size: var(--font-size-sm);
  }
  
  .input-error {
    font-size: var(--font-size-xs);
  }
  
  .custom-input {
    border-radius: var(--border-radius-base) !important;
  }
}
</style>