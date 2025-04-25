/**
 * Error types for categorization
 */
export const ERROR_TYPES = {
  AUTH: 'auth',
  NETWORK: 'network',
  VALIDATION: 'validation',
  SERVER: 'server',
  UNKNOWN: 'unknown'
}

/**
 * Determine error type based on error object
 * @param {Object} error - Error object
 * @returns {string} Error type
 */
export const getErrorType = (error) => {
  if (!error) return ERROR_TYPES.UNKNOWN
  
  // Authentication errors
  if (error.status === 401 || error.status === 403) {
    return ERROR_TYPES.AUTH
  }
  
  // Network errors
  if (!error.status || error.message?.includes('network') || error.message?.includes('connection')) {
    return ERROR_TYPES.NETWORK
  }
  
  // Validation errors
  if (error.status === 400 || error.status === 422) {
    return ERROR_TYPES.VALIDATION
  }
  
  // Server errors
  if (error.status >= 500) {
    return ERROR_TYPES.SERVER
  }
  
  return ERROR_TYPES.UNKNOWN
}

/**
 * Get user-friendly error message
 * @param {Object} error - Error object
 * @returns {string} User-friendly error message
 */
export const getUserFriendlyMessage = (error) => {
  if (!error) return 'An unknown error occurred'
  
  // If error already has a message, use it
  if (error.message) return error.message
  
  // Otherwise, generate based on error type
  const errorType = getErrorType(error)
  
  switch (errorType) {
    case ERROR_TYPES.AUTH:
      return 'Authentication failed. Please log in again.'
    case ERROR_TYPES.NETWORK:
      return 'Network error. Please check your connection and try again.'
    case ERROR_TYPES.VALIDATION:
      return 'Invalid input. Please check your data and try again.'
    case ERROR_TYPES.SERVER:
      return 'Server error. Please try again later.'
    default:
      return 'An unknown error occurred. Please try again.'
  }
}

/**
 * Handle API error with appropriate actions
 * @param {Object} error - Error object
 * @param {Object} options - Handler options
 */
export const handleApiError = (error, options = {}) => {
  const {
    context = '',
    onAuth = null,
    onNetwork = null,
    onValidation = null,
    onServer = null,
    onUnknown = null,
    onAny = null
  } = options
  
  // Get error type
  const errorType = getErrorType(error)
  
  // Call specific handler based on error type
  switch (errorType) {
    case ERROR_TYPES.AUTH:
      if (onAuth && typeof onAuth === 'function') {
        onAuth(error)
      }
      break
    case ERROR_TYPES.NETWORK:
      if (onNetwork && typeof onNetwork === 'function') {
        onNetwork(error)
      }
      break
    case ERROR_TYPES.VALIDATION:
      if (onValidation && typeof onValidation === 'function') {
        onValidation(error)
      }
      break
    case ERROR_TYPES.SERVER:
      if (onServer && typeof onServer === 'function') {
        onServer(error)
      }
      break
    default:
      if (onUnknown && typeof onUnknown === 'function') {
        onUnknown(error)
      }
  }
  
  // Call general handler if provided
  if (onAny && typeof onAny === 'function') {
    onAny(error)
  }
}

export default {
  ERROR_TYPES,
  getErrorType,
  getUserFriendlyMessage,
  handleApiError
}
