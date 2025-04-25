/**
 * Format a date string to a localized time string
 * @param {string} dateString - Date string to format
 * @returns {string} Formatted time string
 */
export const formatTime = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    return date.toLocaleTimeString()
  } catch (error) {
    console.error('Error formatting time:', error)
    return dateString
  }
}

/**
 * Format a date string to a localized date string
 * @param {string} dateString - Date string to format
 * @returns {string} Formatted date string
 */
export const formatDate = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString()
  } catch (error) {
    console.error('Error formatting date:', error)
    return dateString
  }
}

/**
 * Format a date string to a localized date and time string
 * @param {string} dateString - Date string to format
 * @returns {string} Formatted date and time string
 */
export const formatDateTime = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    return date.toLocaleString()
  } catch (error) {
    console.error('Error formatting date and time:', error)
    return dateString
  }
}

/**
 * Get relative time string (e.g. "2 hours ago")
 * @param {string} dateString - Date string to format
 * @returns {string} Relative time string
 */
export const getRelativeTime = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now - date
    
    // Convert to seconds
    const diffSec = Math.floor(diffMs / 1000)
    
    if (diffSec < 60) {
      return 'just now'
    }
    
    // Convert to minutes
    const diffMin = Math.floor(diffSec / 60)
    
    if (diffMin < 60) {
      return `${diffMin} minute${diffMin > 1 ? 's' : ''} ago`
    }
    
    // Convert to hours
    const diffHour = Math.floor(diffMin / 60)
    
    if (diffHour < 24) {
      return `${diffHour} hour${diffHour > 1 ? 's' : ''} ago`
    }
    
    // Convert to days
    const diffDay = Math.floor(diffHour / 24)
    
    if (diffDay < 30) {
      return `${diffDay} day${diffDay > 1 ? 's' : ''} ago`
    }
    
    // Convert to months
    const diffMonth = Math.floor(diffDay / 30)
    
    if (diffMonth < 12) {
      return `${diffMonth} month${diffMonth > 1 ? 's' : ''} ago`
    }
    
    // Convert to years
    const diffYear = Math.floor(diffMonth / 12)
    
    return `${diffYear} year${diffYear > 1 ? 's' : ''} ago`
  } catch (error) {
    console.error('Error getting relative time:', error)
    return dateString
  }
}

export default {
  formatTime,
  formatDate,
  formatDateTime,
  getRelativeTime
}
