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
      <div class="message-bubble" :class="bubbleClass">
        <div class="message-text" v-html="formattedContent"></div>
        <div class="message-actions" v-if="showActions">
          <button class="btn btn-sm btn-link text-muted" @click="copyMessage" title="复制消息">
            <i class="bi bi-clipboard"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const isUser = computed(() => props.message.role === 'user')
    const showActions = ref(false)

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
      // 处理换行符
      let content = props.message.content.replace(/\n/g, '<br>')

      // 处理代码块
      content = content.replace(/```([\s\S]*?)```/g, (match, code) => {
        return `<pre class="code-block"><code>${code}</code></pre>`
      })

      // 处理行内代码
      content = content.replace(/`([^`]+)`/g, '<code>$1</code>')

      return content
    })

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleTimeString()
    }

    // 复制消息内容
    const copyMessage = () => {
      navigator.clipboard.writeText(props.message.content)
        .then(() => {
          alert('消息已复制到剪贴板')
        })
        .catch(err => {
          console.error('复制失败:', err)
        })
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
      copyMessage
    }
  }
}
</script>

<style scoped>
.message {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.message-avatar {
  margin-right: 12px;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  background-color: var(--primary-color);
}

.ai-avatar {
  background-color: var(--secondary-color);
}

.message-content {
  flex: 1;
  max-width: calc(100% - 52px);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-sender {
  font-weight: 500;
  font-size: 0.9rem;
}

.message-time {
  font-size: 0.8rem;
}

.message-bubble {
  position: relative;
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.user-bubble {
  background-color: var(--primary-light);
  border-top-right-radius: 0;
}

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-top-left-radius: 0;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.message-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.message-bubble:hover .message-actions {
  opacity: 1;
}

.message-actions .btn {
  padding: 0;
  font-size: 0.9rem;
}

/* 代码块样式 */
.code-block {
  background-color: var(--neutral-800);
  color: var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 12px;
  margin: 8px 0;
  overflow-x: auto;
}

code {
  font-family: 'Fira Code', monospace;
  background-color: var(--neutral-200);
  padding: 2px 4px;
  border-radius: var(--radius-sm);
  font-size: 0.9em;
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
</style>
