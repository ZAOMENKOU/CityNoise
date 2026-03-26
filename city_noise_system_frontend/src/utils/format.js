// 格式化工具函数

/**
 * 格式化日期
 * @param {Date|string|number} date - 日期对象、字符串或时间戳
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''
  
  const d = new Date(date)
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化时间
 * @param {Date|string|number} date - 日期对象、字符串或时间戳
 * @returns {string} 格式化后的时间字符串，格式：HH:mm:ss
 */
export const formatTime = (date) => {
  return formatDate(date, 'HH:mm:ss')
}

/**
 * 格式化日期时间
 * @param {Date|string|number} date - 日期对象、字符串或时间戳
 * @returns {string} 格式化后的日期时间字符串，格式：YYYY-MM-DD HH:mm:ss
 */
export const formatDateTime = (date) => {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化数字
 * @param {number} num - 数字
 * @param {number} decimals - 小数位数，默认 2
 * @returns {string} 格式化后的数字字符串
 */
export const formatNumber = (num, decimals = 2) => {
  if (isNaN(num)) return '0'
  return Number(num).toFixed(decimals)
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化投诉状态
 * @param {string} status - 状态码
 * @returns {string} 状态文本
 */
export const formatComplaintStatus = (status) => {
  const statusMap = {
    PENDING: '待处理',
    ASSIGNED: '已分配',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
    REOPENED: '已重新打开'
  }
  return statusMap[status] || status
}

/**
 * 格式化功能区类型
 * @param {string} zone - 功能区类型
 * @returns {string} 功能区文本
 */
export const formatFunctionalZone = (zone) => {
  const zoneMap = {
    CLASS_1: '1类区（康复疗养区等）',
    CLASS_2: '2类区（商业金融、居住区等）',
    CLASS_3: '3类区（工业区）',
    CLASS_4A: '4a类区（交通干线两侧）',
    CLASS_4B: '4b类区（铁路干线两侧）'
  }
  return zoneMap[zone] || zone
}

/**
 * 格式化角色
 * @param {string} role - 角色码
 * @returns {string} 角色文本
 */
export const formatRole = (role) => {
  const roleMap = {
    RESIDENT: '居民',
    WORKER: '处理人员',
    ADMIN: '管理员'
  }
  return roleMap[role] || role
}
