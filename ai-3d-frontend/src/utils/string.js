/**
 * String utility functions
 */

/**
 * Truncate a string to a specified length and add ellipsis if needed
 * @param {string} str - String to truncate
 * @param {number} maxLength - Maximum length
 * @param {string} ellipsis - Ellipsis string (default: '...')
 * @returns {string} Truncated string
 */
export const truncate = (str, maxLength, ellipsis = '...') => {
  if (!str) return ''
  
  if (str.length <= maxLength) {
    return str
  }
  
  return str.slice(0, maxLength) + ellipsis
}

/**
 * Capitalize the first letter of a string
 * @param {string} str - String to capitalize
 * @returns {string} Capitalized string
 */
export const capitalize = (str) => {
  if (!str) return ''
  
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * Convert a string to title case (capitalize each word)
 * @param {string} str - String to convert
 * @returns {string} Title case string
 */
export const titleCase = (str) => {
  if (!str) return ''
  
  return str
    .toLowerCase()
    .split(' ')
    .map(word => capitalize(word))
    .join(' ')
}

/**
 * Convert a string to camel case
 * @param {string} str - String to convert
 * @returns {string} Camel case string
 */
export const camelCase = (str) => {
  if (!str) return ''
  
  return str
    .toLowerCase()
    .replace(/[^a-zA-Z0-9]+(.)/g, (_, char) => char.toUpperCase())
}

/**
 * Convert a string to kebab case (hyphen-separated)
 * @param {string} str - String to convert
 * @returns {string} Kebab case string
 */
export const kebabCase = (str) => {
  if (!str) return ''
  
  return str
    .replace(/([a-z])([A-Z])/g, '$1-$2')
    .replace(/[\s_]+/g, '-')
    .toLowerCase()
}

/**
 * Convert a string to snake case (underscore_separated)
 * @param {string} str - String to convert
 * @returns {string} Snake case string
 */
export const snakeCase = (str) => {
  if (!str) return ''
  
  return str
    .replace(/([a-z])([A-Z])/g, '$1_$2')
    .replace(/[\s-]+/g, '_')
    .toLowerCase()
}

/**
 * Strip HTML tags from a string
 * @param {string} html - HTML string
 * @returns {string} Plain text string
 */
export const stripHtml = (html) => {
  if (!html) return ''
  
  return html.replace(/<[^>]*>/g, '')
}

/**
 * Generate a random string
 * @param {number} length - Length of the string
 * @param {string} chars - Characters to use (default: alphanumeric)
 * @returns {string} Random string
 */
export const randomString = (length, chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789') => {
  let result = ''
  
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  
  return result
}

/**
 * Escape HTML special characters
 * @param {string} str - String to escape
 * @returns {string} Escaped string
 */
export const escapeHtml = (str) => {
  if (!str) return ''
  
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

/**
 * Format a number with commas as thousands separators
 * @param {number} num - Number to format
 * @returns {string} Formatted number
 */
export const formatNumber = (num) => {
  if (num === null || num === undefined) return ''
  
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

export default {
  truncate,
  capitalize,
  titleCase,
  camelCase,
  kebabCase,
  snakeCase,
  stripHtml,
  randomString,
  escapeHtml,
  formatNumber
}
