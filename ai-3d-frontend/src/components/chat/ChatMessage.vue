<template>
  <transition :name="isUser ? 'slide-right' : 'slide-left'" appear>
    <div class="message hardware-accelerated" :class="messageClass">
      <div class="message-avatar">
        <div class="avatar-circle hover-lift" :class="avatarClass">
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
        <div class="message-bubble hover-lift" :class="bubbleClass" @mouseenter="showActions = true" @mouseleave="showActions = false">
          <div class="message-text" v-html="formattedContent"></div>
          <div class="message-actions" v-if="showActions">
            <button class="btn btn-sm btn-icon btn-press" @click="copyMessage" title="复制消息">
              <i class="bi" :class="copySuccess ? 'bi-check-lg text-success' : 'bi-clipboard'"></i>
            </button>
            <button class="btn btn-sm btn-icon text-danger btn-press" @click="confirmDelete" title="删除消息">
              <i class="bi bi-trash"></i>
            </button>
            <div class="copy-tooltip" v-if="copySuccess">已复制到剪贴板</div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { computed, ref } from 'vue'
import { marked } from 'marked'
import { useStore } from '@/utils/storeCompat'

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: Object,
      required: true
    }
  },
  emits: ['delete'],
  setup(props, { emit }) {
    const store = useStore()
    const isUser = computed(() => props.message.role === 'user')
    const showActions = ref(false)

    // 配置marked选项
    marked.setOptions({
      gfm: true, // 启用GitHub风格的Markdown
      breaks: true, // 允许回车换行
      silent: true, // 忽略错误
      highlight: function(code, lang) {
        return code; // 简单返回代码，可以在这里集成语法高亮库
      }
    })

    // 消息类型相关的计算属性
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

    // 格式化消息内容
    const formattedContent = computed(() => {
      try {
        // 使用marked库解析Markdown
        const parsed = marked.parse(props.message.content);
        console.log('ChatMessage: Markdown解析成功，消息ID:', props.message.id);
        return parsed;
      } catch (error) {
        console.error('Markdown解析错误:', error)
        // 降级处理
        let content = props.message.content.replace(/\n/g, '<br>')
        // 处理代码块
        content = content.replace(/```([\s\S]*?)```/g, (match, code) => {
          return `<pre class="code-block"><code>${code}</code></pre>`
        })
        // 处理行内代码
        content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
        return content
      }
    })

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleTimeString()
    }

    // 复制消息内容
    const copySuccess = ref(false)
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

    // 删除消息
    const confirmDelete = () => {
      if (confirm('确定要删除这条消息吗？')) {
        emit('delete', props.message.id)
      }
    }

    return {
      messageClass,
      avatarClass,
      avatarIcon,
      senderName,
      bubbleClass,
      formattedContent,
      formatTime,
      showActions,
      copyMessage,
      copySuccess,
      confirmDelete
    }
  }
}
</script>

<style scoped>
.message {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

.message-avatar {
  margin-right: 8px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
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

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.message-sender {
  font-weight: 500;
  font-size: 0.8rem;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
}

.message-bubble {
  position: relative;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}

.message-bubble:hover {
  box-shadow: var(--shadow-md);
}

.user-bubble {
  background-color: var(--primary-light);
  border-top-right-radius: 0;
  border-right: 2px solid var(--primary-color);
}

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-top-left-radius: 0;
  border-left: 2px solid var(--secondary-color);
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.4;
  font-size: 0.95rem;
}

.message-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: var(--radius-full);
  padding: 2px;
}

.message-bubble:hover .message-actions {
  opacity: 1;
}

.btn-icon {
  width: 24px;
  height: 24px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background-color: var(--bg-tertiary);
  border: none;
  transition: background-color 0.2s ease;
  font-size: 0.8rem;
}

.btn-icon:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.copy-tooltip {
  position: absolute;
  right: 30px;
  top: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  padding: 3px 6px;
  border-radius: var(--radius-md);
  font-size: 0.7rem;
  box-shadow: var(--shadow-sm);
  animation: fadeIn 0.3s ease;
  white-space: nowrap;
}

.copy-tooltip::after {
  content: '';
  position: absolute;
  top: 50%;
  right: -5px;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid var(--bg-primary);
}

.message-actions .btn {
  padding: 0;
  font-size: 0.8rem;
}

/* 代码块样式 */
.code-block {
  background-color: var(--neutral-800);
  color: var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 8px 10px;
  margin: 6px 0;
  overflow-x: auto;
  font-size: 0.85rem;
  line-height: 1.4;
}

code {
  font-family: 'Fira Code', monospace, Consolas, Monaco, 'Andale Mono', monospace;
  background-color: var(--neutral-200);
  padding: 1px 3px;
  border-radius: var(--radius-sm);
  font-size: 0.85em;
}

.code-block code {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
  display: block;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-message {
  animation: slideInRight 0.3s ease;
}

.ai-message {
  animation: slideInLeft 0.3s ease;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 消息渲染过渡动画 */
.message-fade-enter-active,
.message-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
