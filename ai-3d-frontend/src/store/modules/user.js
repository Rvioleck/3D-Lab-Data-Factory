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
      const response = await login(credentials)
      if (response.code === 0) {
        commit('SET_CURRENT_USER', response.data)
        localStorage.setItem('token', 'logged-in') // 简化处理，实际应存储JWT
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '登录失败')
      }
    } catch (error) {
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
      const response = await getLoginUser()
      if (response.code === 0) {
        commit('SET_CURRENT_USER', response.data)
        return Promise.resolve(response.data)
      } else {
        commit('SET_CURRENT_USER', null)
        localStorage.removeItem('token')
        return Promise.reject(response.message || '获取用户信息失败')
      }
    } catch (error) {
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
