// 配置
const config = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
}

// 处理请求
const request = async (url, options = {}) => {
  // 构建完整URL
  const fullUrl = `${config.baseURL}${url}`
  
  // 设置默认选项
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json'
    },
    timeout: config.timeout
  }
  
  // 合并选项
  const mergedOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  }
  
  // 添加token
  const token = localStorage.getItem('token')
  if (token) {
    mergedOptions.headers['Authorization'] = `Bearer ${token}`
  }
  
  // 处理超时
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), mergedOptions.timeout)
  
  mergedOptions.signal = controller.signal
  
  try {
    // 发送请求
    const response = await fetch(fullUrl, mergedOptions)
    clearTimeout(timeoutId)
    
    // 检查响应状态
    if (!response.ok) {
      const errorMessage = `HTTP error! status: ${response.status}`
      console.error(errorMessage)
      
      // 处理不同的错误状态码
      switch (response.status) {
        case 401:
          console.error('未授权，请重新登录')
          break
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('请求地址不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`错误状态码: ${response.status}`)
      }
      
      throw new Error(errorMessage)
    }
    
    // 解析响应数据
    const data = await response.json()
    
    // 检查响应状态码
    if (data.code !== 200) {
      const errorMessage = data.message || '请求失败'
      console.error('响应错误:', errorMessage)
      throw new Error(errorMessage)
    }
    
    return data
  } catch (error) {
    clearTimeout(timeoutId)
    
    if (error.name === 'AbortError') {
      console.error('请求超时')
      throw new Error('请求超时')
    }
    
    console.error('请求错误:', error)
    throw error
  }
}

// 便捷方法
request.get = (url, params = {}) => {
  // 构建查询字符串
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, value)
    }
  })
  
  const queryString = searchParams.toString()
  const fullUrl = queryString ? `${url}?${queryString}` : url
  
  return request(fullUrl, {
    method: 'GET'
  })
}

request.post = (url, data = {}) => {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

request.put = (url, data = {}) => {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

request.delete = (url) => {
  return request(url, {
    method: 'DELETE'
  })
}

export default request
