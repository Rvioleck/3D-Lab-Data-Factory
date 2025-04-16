import { login, register, getLoginUser } from '../../api/user'

const state = {
  currentUser: null,
  token: localStorage.getItem('token') || null
}

const getters = {
  isLoggedIn: state => !!state.currentUser,
  currentUser: state => state.currentUser
}

const actions = {
  async login({ commit }, credentials) {
    try {
      console.log('开始登录请求:', credentials.userAccount)
      const response = await login(credentials)

      if (response.code === 0) {
        console.log('登录成功，设置用户状态:', response.data)
        commit('SET_CURRENT_USER', response.data)
        localStorage.setItem('token', 'logged-in') // 简化处理，实际应存储JWT
        return Promise.resolve(response.data)
      } else {
        console.error('登录失败，服务器返回错误:', response.message)
        return Promise.reject(response.message || '登录失败')
      }
    } catch (error) {
      console.error('登录请求异常:', error)
      return Promise.reject(error.message || '登录失败')
    }
  },

  async register({ commit }, userData) {
    try {
      const response = await register(userData)
      if (response.code === 0) {
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '注册失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '注册失败')
    }
  },

  async fetchCurrentUser({ commit }) {
    try {
      console.log('开始获取当前登录用户信息')
      const response = await getLoginUser()

      if (response.code === 0) {
        console.log('获取用户信息成功:', response.data)
        commit('SET_CURRENT_USER', response.data)
        return Promise.resolve(response.data)
      } else {
        console.error('获取用户信息失败，服务器返回错误:', response.message)
        commit('SET_CURRENT_USER', null)
        localStorage.removeItem('token')
        return Promise.reject(response.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息请求异常:', error)
      commit('SET_CURRENT_USER', null)
      localStorage.removeItem('token')
      return Promise.reject(error.message || '获取用户信息失败')
    }
  },

  logout({ commit }) {
    commit('SET_CURRENT_USER', null)
    localStorage.removeItem('token')
  }
}

const mutations = {
  SET_CURRENT_USER(state, user) {
    state.currentUser = user
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
