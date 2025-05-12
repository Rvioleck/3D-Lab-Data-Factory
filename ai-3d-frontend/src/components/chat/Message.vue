<template>
  <div class="message" :class="messageClass">
    <div class="message-avatar">
      <div class="avatar-circle" :class="avatarClass">
        <i class="bi" :class="avatarIcon"></i>
      </div>
    </div>
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">{{ senderName }}</span>
        <span class="message-time text-muted small">
          {{ formatTime(message.createTime) }}
        </span>
      </div>

      <!-- 编辑模式 -->
      <div v-if="isEditing" class="message-bubble edit-mode" :class="bubbleClass">
        <textarea
          ref="editTextarea"
          v-model="editContent"
          class="edit-textarea"
          @keydown.esc="cancelEdit"
        ></textarea>
        <div class="edit-actions">
          <button class="btn btn-sm btn-primary" @click="saveEdit">保存</button>
          <button class="btn btn-sm btn-outline-secondary" @click="cancelEdit">取消</button>
        </div>
      </div>

      <!-- 显示模式 -->
      <div
        v-else
        class="message-bubble"
        :class="bubbleClass"
        @mouseenter="showActions = true"
        @mouseleave="showActions = false"
      >
        <div class="message-text" v-html="formattedContent"></div>
        <div class="message-actions" v-if="showActions">
          <button
            v-if="isUser"
            class="action-button"
            @click="startEdit"
            title="编辑消息"
          >
            <i class="bi bi-pencil"></i>
          </button>
          <button class="action-button" @click="copyMessage" title="复制消息">
            <i class="bi" :class="copySuccess ? 'bi-check-lg text-success' : 'bi-clipboard'"></i>
          </button>
          <button class="action-button text-danger" @click="confirmDelete" title="删除消息">
            <i class="bi bi-trash"></i>
          </button>
          <div class="copy-tooltip" v-if="copySuccess">已复制</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['delete', 'edit'])

const showActions = ref(false)
const copySuccess = ref(false)
const isEditing = ref(false)
const editContent = ref('')

// Configure marked options with syntax highlighting
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (e) {
        console.error('Highlight error:', e)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,        // 将\n转换为<br>
  gfm: true,           // 使用GitHub风格的Markdown
  headerIds: true,     // 为标题添加id
  mangle: false,       // 不转义HTML
  sanitize: false,     // 不过滤HTML标签
  smartLists: true,    // 使用更智能的列表行为
  smartypants: false,  // 不使用更智能的标点符号
  xhtml: false         // 不使用自闭合标签
})

// Message type related computed properties
const isUser = computed(() => props.message.role === 'user')

const messageClass = computed(() => ({
  'user-message': isUser.value,
  'ai-message': !isUser.value
}))

const avatarClass = computed(() => ({
  'user-avatar': isUser.value,
  'ai-avatar': !isUser.value
}))

const avatarIcon = computed(() =>
  isUser.value ? 'bi-person-fill' : 'bi-robot'
)

const senderName = computed(() =>
  isUser.value ? '用户' : 'AI 助手'
)

const bubbleClass = computed(() => ({
  'user-bubble': isUser.value,
  'ai-bubble': !isUser.value
}))

// Format message content with markdown
const formattedContent = computed(() => {
  try {
    // 确保内容是字符串
    const contentStr = String(props.message.content || '')

    // 使用marked解析markdown
    const parsed = marked.parse(contentStr)

    // 如果解析成功，返回解析后的内容
    return parsed
  } catch (error) {
    console.error('Markdown解析错误:', error)

    // 回退处理：手动处理基本格式
    let content = String(props.message.content || '')

    // 保留换行符
    content = content.replace(/\n/g, '<br>')

    // 处理代码块
    content = content.replace(/```([\s\S]*?)```/g, (match, code) => {
      return `<pre class="code-block"><code>${code}</code></pre>`
    })

    // 处理内联代码
    content = content.replace(/`([^`]+)`/g, '<code>$1</code>')

    return content
  }
})

// Format time
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Copy message content
const copyMessage = () => {
  navigator.clipboard.writeText(props.message.content)
    .then(() => {
      copySuccess.value = true
      setTimeout(() => {
        copySuccess.value = false
      }, 2000)
    })
    .catch(err => {
      console.error('复制失败:', err)
    })
}

// Delete message
const confirmDelete = () => {
  if (confirm('确定要删除这条消息吗？')) {
    emit('delete', props.message.id)
  }
}

// Edit message functions
const startEdit = () => {
  editContent.value = props.message.content
  isEditing.value = true
  // 在下一个 tick 后聚焦文本区域
  setTimeout(() => {
    const textarea = document.querySelector('.edit-textarea')
    if (textarea) {
      textarea.focus()
      // 将光标放在文本末尾
      textarea.selectionStart = textarea.selectionEnd = textarea.value.length
    }
  }, 50)
}

const saveEdit = () => {
  if (editContent.value.trim() === '') return
  emit('edit', props.message.id, editContent.value)
  isEditing.value = false
}

const cancelEdit = () => {
  isEditing.value = false
  editContent.value = ''
}
</script>

<style>
/* 全局样式，确保代码高亮正常工作 */
.hljs {
  display: block;
  overflow-x: auto;
  padding: 0.5em;
  color: #333;
  background: #f8f8f8;
}
</style>

<style scoped>
.message {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  margin-right: 8px;
}

.user-message .message-avatar {
  margin-right: 0;
  margin-left: 8px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease;
}

.message:hover .avatar-circle {
  transform: scale(1.05);
}

.user-avatar {
  background-color: var(--primary-color);
}

.ai-avatar {
  background-color: var(--secondary-color);
}

.message-content {
  flex: 1;
  max-width: calc(100% - 40px);
}

.user-message .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
  width: 100%;
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease;
}

.message-bubble:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.user-bubble {
  background-color: var(--primary-light);
  color: var(--text-primary);
  border-radius: 12px 0 12px 12px;
}

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-radius: 0 12px 12px 12px;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  line-height: 1.6;
  width: 100%;
}

.message-actions {
  position: absolute;
  top: -24px;
  right: 0;
  display: flex;
  gap: 4px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 2px 4px;
  border-radius: 4px;
  backdrop-filter: blur(2px);
  z-index: 10;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.user-message .message-actions {
  right: 0;
  left: auto;
}

.action-button {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 3px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.action-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

.action-button.text-danger:hover {
  color: var(--danger-color, #dc3545);
}

.copy-tooltip {
  position: absolute;
  top: -20px;
  right: 0;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 0.7rem;
  white-space: nowrap;
}

/* 编辑模式样式 */
.edit-mode {
  display: flex;
  flex-direction: column;
  padding: 4px;
}

.edit-textarea {
  width: 100%;
  min-height: 80px;
  padding: 6px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: inherit;
  font-size: inherit;
  line-height: 1.5;
  resize: vertical;
  margin-bottom: 6px;
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

.edit-actions button {
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
}

:deep(.code-block) {
  background: var(--bg-secondary, #f6f8fa);
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
  overflow-x: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

:deep(code) {
  background: var(--bg-secondary, #f6f8fa);
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
}

:deep(pre) {
  margin: 1em 0;
  padding: 1em;
  overflow-x: auto;
  background: var(--bg-secondary, #f6f8fa);
  border-radius: 4px;
}

:deep(p) {
  margin: 0.5em 0;
}

:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.25;
}

:deep(ul), :deep(ol) {
  padding-left: 2em;
  margin: 0.5em 0;
}

:deep(li) {
  margin: 0.25em 0;
}

:deep(blockquote) {
  margin: 1em 0;
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
}

:deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

:deep(th), :deep(td) {
  padding: 0.5em;
  border: 1px solid #dfe2e5;
}

:deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
