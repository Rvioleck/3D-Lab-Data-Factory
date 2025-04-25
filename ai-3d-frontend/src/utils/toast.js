/**
 * Global toast utility for showing notifications
 * This utility provides a consistent way to show toast notifications
 * throughout the application.
 */

// Default toast options
const defaultOptions = {
  duration: 3000,
  position: 'top-right',
  type: 'info'
}

// Toast instance reference
let toastInstance = null

/**
 * Initialize toast utility with a toast component instance
 * @param {Object} instance - Toast component instance
 */
export const initToast = (instance) => {
  toastInstance = instance
  
  // Add to window for global access
  window.$toast = createToastAPI()
}

/**
 * Create toast API object
 * @returns {Object} Toast API
 */
const createToastAPI = () => {
  return {
    /**
     * Show a success toast
     * @param {string} message - Toast message
     * @param {Object} options - Toast options
     */
    success: (message, options = {}) => {
      showToast(message, { ...options, type: 'success' })
    },
    
    /**
     * Show an error toast
     * @param {string} message - Toast message
     * @param {Object} options - Toast options
     */
    error: (message, options = {}) => {
      showToast(message, { ...options, type: 'error' })
    },
    
    /**
     * Show a warning toast
     * @param {string} message - Toast message
     * @param {Object} options - Toast options
     */
    warning: (message, options = {}) => {
      showToast(message, { ...options, type: 'warning' })
    },
    
    /**
     * Show an info toast
     * @param {string} message - Toast message
     * @param {Object} options - Toast options
     */
    info: (message, options = {}) => {
      showToast(message, { ...options, type: 'info' })
    }
  }
}

/**
 * Show a toast notification
 * @param {string} message - Toast message
 * @param {Object} options - Toast options
 */
const showToast = (message, options = {}) => {
  if (!toastInstance) {
    console.warn('Toast component not initialized')
    return
  }
  
  const finalOptions = { ...defaultOptions, ...options }
  
  toastInstance.show(message, finalOptions)
}

export default {
  initToast
}
