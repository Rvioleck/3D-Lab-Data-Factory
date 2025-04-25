/**
 * Validation utility functions
 */

/**
 * Check if a value is empty (null, undefined, empty string, empty array, empty object)
 * @param {*} value - Value to check
 * @returns {boolean} True if empty, false otherwise
 */
export const isEmpty = (value) => {
  if (value === null || value === undefined) {
    return true
  }
  
  if (typeof value === 'string') {
    return value.trim() === ''
  }
  
  if (Array.isArray(value)) {
    return value.length === 0
  }
  
  if (typeof value === 'object') {
    return Object.keys(value).length === 0
  }
  
  return false
}

/**
 * Validate email format
 * @param {string} email - Email to validate
 * @returns {boolean} True if valid, false otherwise
 */
export const isValidEmail = (email) => {
  if (isEmpty(email)) return false
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * Validate password strength
 * @param {string} password - Password to validate
 * @param {Object} options - Validation options
 * @param {number} options.minLength - Minimum length (default: 8)
 * @param {boolean} options.requireUppercase - Require uppercase letter (default: true)
 * @param {boolean} options.requireLowercase - Require lowercase letter (default: true)
 * @param {boolean} options.requireNumber - Require number (default: true)
 * @param {boolean} options.requireSpecial - Require special character (default: true)
 * @returns {Object} Validation result with isValid flag and message
 */
export const validatePassword = (password, options = {}) => {
  const {
    minLength = 8,
    requireUppercase = true,
    requireLowercase = true,
    requireNumber = true,
    requireSpecial = true
  } = options
  
  if (isEmpty(password)) {
    return {
      isValid: false,
      message: 'Password cannot be empty'
    }
  }
  
  if (password.length < minLength) {
    return {
      isValid: false,
      message: `Password must be at least ${minLength} characters long`
    }
  }
  
  if (requireUppercase && !/[A-Z]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one uppercase letter'
    }
  }
  
  if (requireLowercase && !/[a-z]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one lowercase letter'
    }
  }
  
  if (requireNumber && !/[0-9]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one number'
    }
  }
  
  if (requireSpecial && !/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one special character'
    }
  }
  
  return {
    isValid: true,
    message: 'Password is valid'
  }
}

/**
 * Validate URL format
 * @param {string} url - URL to validate
 * @returns {boolean} True if valid, false otherwise
 */
export const isValidUrl = (url) => {
  if (isEmpty(url)) return false
  
  try {
    new URL(url)
    return true
  } catch (error) {
    return false
  }
}

/**
 * Validate phone number format (simple validation)
 * @param {string} phone - Phone number to validate
 * @returns {boolean} True if valid, false otherwise
 */
export const isValidPhone = (phone) => {
  if (isEmpty(phone)) return false
  
  // Simple validation - at least 10 digits
  const phoneRegex = /^\+?[0-9]{10,15}$/
  return phoneRegex.test(phone.replace(/[\s-]/g, ''))
}

/**
 * Validate form data against a schema
 * @param {Object} data - Form data to validate
 * @param {Object} schema - Validation schema
 * @returns {Object} Validation result with errors object
 */
export const validateForm = (data, schema) => {
  const errors = {}
  
  Object.keys(schema).forEach(field => {
    const value = data[field]
    const rules = schema[field]
    
    if (rules.required && isEmpty(value)) {
      errors[field] = rules.requiredMessage || 'This field is required'
      return
    }
    
    if (!isEmpty(value)) {
      if (rules.minLength && value.length < rules.minLength) {
        errors[field] = `Minimum length is ${rules.minLength} characters`
      }
      
      if (rules.maxLength && value.length > rules.maxLength) {
        errors[field] = `Maximum length is ${rules.maxLength} characters`
      }
      
      if (rules.pattern && !rules.pattern.test(value)) {
        errors[field] = rules.patternMessage || 'Invalid format'
      }
      
      if (rules.validator && typeof rules.validator === 'function') {
        const validatorResult = rules.validator(value, data)
        if (validatorResult !== true) {
          errors[field] = validatorResult
        }
      }
    }
  })
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  }
}

export default {
  isEmpty,
  isValidEmail,
  validatePassword,
  isValidUrl,
  isValidPhone,
  validateForm
}
