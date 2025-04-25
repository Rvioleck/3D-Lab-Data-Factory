import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, getLoginUser, logout as apiLogout, updateUserProfile } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const loading = ref(false)
  const error = ref(null)

  // Computed
  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.userRole === 'admin')
  const userName = computed(() => user.value?.userName || user.value?.userAccount || '')

  // Actions
  /**
   * Login user
   * @param {Object} credentials - User credentials
   * @returns {Promise} Login result
   */
  async function login(credentials) {
    loading.value = true
    error.value = null

    try {
      const response = await apiLogin(credentials)

      if (response.code === 0) {
        user.value = response.data

        // Store token if provided
        if (response.data.token) {
          token.value = response.data.token
          localStorage.setItem('token', response.data.token)
        } else {
          // Fallback for testing environments
          localStorage.setItem('token', 'logged-in')
        }

        return response
      } else {
        throw { message: response.message || 'Login failed' }
      }
    } catch (err) {
      error.value = err.message || 'Login failed'
      console.error('Login error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Register new user
   * @param {Object} userData - User registration data
   * @returns {Promise} Registration result
   */
  async function register(userData) {
    loading.value = true
    error.value = null

    try {
      const response = await apiRegister(userData)

      if (response.code === 0) {
        return response
      } else {
        throw { message: response.message || 'Registration failed' }
      }
    } catch (err) {
      error.value = err.message || 'Registration failed'
      console.error('Registration error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch current user data
   * @returns {Promise} User data
   */
  async function fetchCurrentUser() {
    loading.value = true
    error.value = null

    try {
      const response = await getLoginUser()

      if (response.code === 0) {
        user.value = response.data
        return response.data
      } else {
        throw { message: response.message || 'Failed to get user data' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to get user data'
      console.error('Fetch user error:', err)

      // Clear user data on auth error
      if (err.status === 401 || err.code === 40100) {
        clearUserData()
      }

      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Logout user
   * @returns {Promise} Logout result
   */
  async function logout() {
    loading.value = true
    error.value = null

    try {
      const response = await apiLogout()
      clearUserData()
      return response
    } catch (err) {
      error.value = err.message || 'Logout failed'
      console.error('Logout error:', err)
      // Even if API call fails, clear user data
      clearUserData()
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Update user profile
   * @param {Object} userData - User profile data
   * @returns {Promise} Update result
   */
  async function updateProfile(userData) {
    loading.value = true
    error.value = null

    try {
      const response = await updateUserProfile(userData)

      if (response.code === 0) {
        // Update local user data
        user.value = { ...user.value, ...userData }
        return response.data
      } else {
        throw { message: response.message || 'Failed to update profile' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to update profile'
      console.error('Update profile error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Clear user data (on logout or auth error)
   */
  function clearUserData() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  // For backward compatibility
  function setUser(userData) {
    user.value = userData
    if (userData.token) {
      token.value = userData.token
      localStorage.setItem('token', userData.token)
    }
  }

  function clearUser() {
    clearUserData()
  }

  return {
    // State
    user,
    token,
    loading,
    error,

    // Computed
    isLoggedIn,
    isAdmin,
    userName,

    // Actions
    login,
    register,
    fetchCurrentUser,
    logout,
    updateProfile,
    clearUserData,
    // Backward compatibility
    setUser,
    clearUser
  }
})
