// 验证工具函数

/**
 * 验证是否为空
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为空
 */
export const isEmpty = (value) => {
  if (value === null || value === undefined) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

/**
 * 验证是否为邮箱
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否为邮箱
 */
export const isEmail = (value) => {
  if (isEmpty(value)) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(value)
}

/**
 * 验证是否为手机号
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否为手机号
 */
export const isPhone = (value) => {
  if (isEmpty(value)) return false
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(value)
}

/**
 * 验证是否为数字
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为数字
 */
export const isNumber = (value) => {
  return !isNaN(Number(value)) && isFinite(value)
}

/**
 * 验证是否为整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为整数
 */
export const isInteger = (value) => {
  if (!isNumber(value)) return false
  return Number(value) === parseInt(value)
}

/**
 * 验证是否为正整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为正整数
 */
export const isPositiveInteger = (value) => {
  if (!isInteger(value)) return false
  return Number(value) > 0
}

/**
 * 验证是否为有效的URL
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否为有效的URL
 */
export const isUrl = (value) => {
  if (isEmpty(value)) return false
  try {
    new URL(value)
    return true
  } catch {
    return false
  }
}

/**
 * 验证字符串长度
 * @param {string} value - 要验证的值
 * @param {number} min - 最小长度
 * @param {number} max - 最大长度
 * @returns {boolean} 长度是否在范围内
 */
export const isLength = (value, min, max) => {
  if (isEmpty(value)) return min === 0
  const length = value.trim().length
  return length >= min && length <= max
}

/**
 * 验证数字范围
 * @param {number} value - 要验证的值
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @returns {boolean} 是否在范围内
 */
export const isRange = (value, min, max) => {
  if (!isNumber(value)) return false
  const num = Number(value)
  return num >= min && num <= max
}

/**
 * 验证密码强度
 * @param {string} value - 要验证的密码
 * @returns {boolean} 密码强度是否符合要求
 */
export const isStrongPassword = (value) => {
  if (isEmpty(value)) return false
  // 至少8位，包含字母和数字
  return value.length >= 8 && /[a-zA-Z]/.test(value) && /[0-9]/.test(value)
}

/**
 * 验证身份证号
 * @param {string} value - 要验证的身份证号
 * @returns {boolean} 是否为有效的身份证号
 */
export const isIdCard = (value) => {
  if (isEmpty(value)) return false
  const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  return idCardRegex.test(value)
}

/**
 * 表单验证规则
 */
export const rules = {
  required: (message = '此字段不能为空') => ({
    required: true,
    message,
    trigger: ['blur', 'change']
  }),
  email: (message = '请输入有效的邮箱地址') => ({
    validator: (rule, value, callback) => {
      if (isEmpty(value)) {
        callback()
      } else if (!isEmail(value)) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  }),
  phone: (message = '请输入有效的手机号') => ({
    validator: (rule, value, callback) => {
      if (isEmpty(value)) {
        callback()
      } else if (!isPhone(value)) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  }),
  length: (min, max, message = `长度应在${min}-${max}之间`) => ({
    validator: (rule, value, callback) => {
      if (isEmpty(value)) {
        callback()
      } else if (!isLength(value, min, max)) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  }),
  number: (message = '请输入数字') => ({
    validator: (rule, value, callback) => {
      if (isEmpty(value)) {
        callback()
      } else if (!isNumber(value)) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  }),
  range: (min, max, message = `值应在${min}-${max}之间`) => ({
    validator: (rule, value, callback) => {
      if (isEmpty(value)) {
        callback()
      } else if (!isRange(value, min, max)) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  })
}
