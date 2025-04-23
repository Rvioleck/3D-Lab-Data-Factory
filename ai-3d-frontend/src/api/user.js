import axios from 'axios'

const API_URL = '/api'

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // 允许跨域请求携带cookie
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 可以在这里添加认证信息等
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    // 如果响应成功，直接返回数据
    return response
  },
  error => {
    // 处理错误响应
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 用户登录
export const login = async (credentials) => {
  try {
    console.log('发送登录请求:', credentials.userAccount)
    const response = await apiClient.post('/user/login', credentials, {
      withCredentials: true, // 确保发送请求时带上cookie
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('登录响应:', response.data)
    return response.data
  } catch (error) {
    console.error('登录失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 用户注册
export const register = async (userData) => {
  try {
    const response = await apiClient.post('/user/register', userData)
    return response.data
  } catch (error) {
    console.error('注册失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取当前登录用户信息
export const getLoginUser = async () => {
  try {
    console.log('发送获取当前登录用户信息请求')
    const response = await apiClient.post('/user/get/login', {}, {
      withCredentials: true, // 确保发送请求时带上cookie
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('获取用户信息响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 用户退出
export const logout = async () => {
  try {
    console.log('发送用户退出请求')
    const response = await apiClient.post('/user/logout', {}, {
      withCredentials: true, // 确保发送请求时带上cookie
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('用户退出响应:', response.data)
    return response.data
  } catch (error) {
    console.error('用户退出失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 更新用户个人资料
export const updateUserProfile = async (profileData) => {
  try {
    console.log('发送更新个人资料请求')
    const response = await apiClient.post('/user/profile/update', profileData, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('更新个人资料响应:', response.data)
    return response.data
  } catch (error) {
    console.error('更新个人资料失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 上传用户头像
export const uploadUserAvatar = async (file) => {
  try {
    console.log('发送上传头像请求')
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/user/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })
    console.log('上传头像响应:', response.data)
    return response.data
  } catch (error) {
    console.error('上传头像失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 修改用户密码
export const updateUserPassword = async (passwordData) => {
  try {
    console.log('发送修改密码请求')
    const response = await apiClient.post('/user/password/update', passwordData, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('修改密码响应:', response.data)
    return response.data
  } catch (error) {
    console.error('修改密码失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// ===== 管理员功能 ===== //

// 分页获取用户列表（管理员）
export const getUserList = async (queryParams) => {
  try {
    console.log('发送获取用户列表请求:', queryParams)
    const response = await apiClient.post('/user/list/page', queryParams, {
      withCredentials: true
    })
    console.log('获取用户列表响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取所有用户（管理员）
export const getAllUsers = async () => {
  try {
    console.log('发送获取所有用户请求')
    const response = await apiClient.post('/user/list', {}, {
      withCredentials: true
    })
    console.log('获取所有用户响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取所有用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 根据ID获取用户（管理员）
export const getUserById = async (id) => {
  try {
    console.log('发送获取用户请求:', id)
    const response = await apiClient.post('/user/get', { id }, {
      withCredentials: true
    })
    console.log('获取用户响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 根据ID获取用户详情（管理员）
export const getUserDetailById = async (id) => {
  try {
    console.log('发送获取用户详情请求:', id)
    const response = await apiClient.post('/user/detail', { id }, {
      withCredentials: true
    })
    console.log('获取用户详情响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取用户详情失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 创建用户（管理员）
export const createUser = async (userData) => {
  try {
    console.log('发送创建用户请求:', userData)
    const response = await apiClient.post('/user/create', userData, {
      withCredentials: true
    })
    console.log('创建用户响应:', response.data)
    return response.data
  } catch (error) {
    console.error('创建用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 更新用户（管理员）
export const updateUser = async (userData) => {
  try {
    console.log('发送更新用户请求:', userData)
    const response = await apiClient.post('/user/update', userData, {
      withCredentials: true
    })
    console.log('更新用户响应:', response.data)
    return response.data
  } catch (error) {
    console.error('更新用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 删除用户（管理员）
export const deleteUser = async (id) => {
  try {
    console.log('发送删除用户请求:', id)
    const response = await apiClient.post('/user/delete', { id }, {
      withCredentials: true
    })
    console.log('删除用户响应:', response.data)
    return response.data
  } catch (error) {
    console.error('删除用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
