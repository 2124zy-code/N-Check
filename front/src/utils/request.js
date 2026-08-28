import { message } from 'ant-design-vue'

const TOKEN_KEY = 'ncheck_auth_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 统一网络请求封装
 */
export async function request(url, options = {}) {
  const token = getToken()
  const defaultHeaders = {
    'Content-Type': 'application/json',
  }

  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...(options.headers || {}),
    },
  }

  // 自动兼容环境变量中配置的独立后端地址 (如 Railway 独立后端域名)
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const apiPath = url.startsWith('/api') ? url : `/api/v1${url.startsWith('/') ? url : '/' + url}`
  const fullUrl = baseUrl ? `${baseUrl.replace(/\/$/, '')}${apiPath}` : apiPath

  try {
    const res = await fetch(fullUrl, config)

    // 401 统一拦截
    if (res.status === 401) {
      removeToken()
      message.error('登录状态已失效，请重新登录')
      if (window.location.hash !== '#/login') {
        window.location.hash = '#/login'
      }
      throw new Error('Unauthorized')
    }

    const data = await res.json()

    if (data.code !== 200) {
      const errMsg = data.message || '请求失败'
      message.error(errMsg)
      throw new Error(errMsg)
    }

    return data.data
  } catch (error) {
    if (error.message !== 'Unauthorized') {
      console.error(`[API Error] ${url}:`, error)
    }
    throw error
  }
}

request.get = (url, params) => {
  let fullUrl = url
  if (params) {
    const query = new URLSearchParams()
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        query.append(key, params[key])
      }
    })
    const queryString = query.toString()
    if (queryString) {
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString
    }
  }
  return request(fullUrl, { method: 'GET' })
}

request.post = (url, data) => {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

request.put = (url, data) => {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

request.patch = (url, data) => {
  return request(url, {
    method: 'PATCH',
    body: JSON.stringify(data || {}),
  })
}

request.delete = (url) => {
  return request(url, {
    method: 'DELETE',
  })
}

export default request
