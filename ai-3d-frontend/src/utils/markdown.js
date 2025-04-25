import { marked } from 'marked'

// Configure marked options
marked.setOptions({
  gfm: true, // GitHub flavored markdown
  breaks: true, // Convert line breaks to <br>
  silent: true, // Ignore errors
  headerIds: false, // Don't add IDs to headers
  mangle: false // Don't mangle email addresses
})

/**
 * Parse markdown to HTML
 * @param {string} markdown - Markdown content
 * @returns {string} HTML content
 */
export const parseMarkdown = (markdown) => {
  if (!markdown) return ''
  
  try {
    return marked.parse(markdown)
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return fallbackParse(markdown)
  }
}

/**
 * Fallback markdown parser for when marked fails
 * @param {string} markdown - Markdown content
 * @returns {string} Basic HTML content
 */
export const fallbackParse = (markdown) => {
  if (!markdown) return ''
  
  // Replace line breaks
  let html = markdown.replace(/\n/g, '<br>')
  
  // Handle code blocks
  html = html.replace(/```([\s\S]*?)```/g, (match, code) => {
    return `<pre class="code-block"><code>${code}</code></pre>`
  })
  
  // Handle inline code
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // Handle bold text
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // Handle italic text
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  
  return html
}

export default {
  parseMarkdown,
  fallbackParse
}
