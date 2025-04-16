<template>
  <div class="message ai-message">
    <div class="message-avatar">
      <div class="avatar-circle ai-avatar">
        <i class="bi bi-robot"></i>
      </div>
    </div>
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">AI 助手</span>
        <span class="message-time text-muted small">
          {{ currentTime }}
        </span>
      </div>
      <div class="message-bubble ai-bubble">
        <div class="message-text streaming-text">
          <span v-html="formattedContent"></span>
          <span class="typing-cursor"></span>
        </div>
        <div class="typing-indicator">
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'

export default {
  name: 'StreamingResponse',
  props: {
    content: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const currentTime = ref(new Date().toLocaleTimeString())
    let timeInterval = null

    // 格式化消息内容
    const formattedContent = computed(() => {
      // 处理换行符
      let content = props.content.replace(/\n/g, '<br>')

      // 处理代码块
      content = content.replace(/```([\s\S]*?)```/g, (match, code) => {
        return `<pre class="code-block"><code>${code}</code></pre>`
      })

      // 处理行内代码
      content = content.replace(/`([^`]+)`/g, '<code>$1</code>')

      return content
    })

    // 组件挂载时启动定时器
    onMounted(() => {
      // 每秒更新时间
      timeInterval = setInterval(() => {
        currentTime.value = new Date().toLocaleTimeString()
      }, 1000)
    })

    // 组件卸载前清除定时器
    onBeforeUnmount(() => {
      if (timeInterval) {
        clearInterval(timeInterval)
      }
    })

    return {
      currentTime,
      formattedContent
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

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-top-left-radius: 0;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

/* 打字机效果 */
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background-color: currentColor;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  from, to { opacity: 1; }
  50% { opacity: 0; }
}

/* 正在输入指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  height: 20px;
}

.typing-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--neutral-400);
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
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
