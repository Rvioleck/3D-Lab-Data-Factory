<template>
  <transition name="slide-left" appear>
    <div class="message ai-message hardware-accelerated">
      <div class="message-avatar">
        <div class="avatar-circle ai-avatar pulse-animation">
          <i class="bi bi-robot"></i>
        </div>
      </div>
      <div class="message-content">
        <div class="message-header">
          <div class="d-flex align-items-center">
            <span class="message-sender">AI 助手</span>
            <span class="badge bg-primary ms-2 streaming-badge pulse-animation">
              <i class="bi bi-lightning-charge"></i> 正在生成
            </span>
          </div>
          <span class="message-time text-muted small">
            {{ currentTime }}
          </span>
        </div>
        <div class="message-bubble ai-bubble">
          <div class="message-text streaming-text hardware-accelerated">
            <div ref="markdownContent" v-html="formattedContent"></div>
            <span class="typing-cursor-optimized"></span>
          </div>
          <div class="typing-indicator">
            <span class="typing-dot-optimized"></span>
            <span class="typing-dot-optimized"></span>
            <span class="typing-dot-optimized"></span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { marked } from 'marked'

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
    const markdownContent = ref(null)
    let timeInterval = null

    // 配置marked选项
    marked.setOptions({
      gfm: true, // 启用GitHub风格的Markdown
      breaks: true, // 允许回车换行
      silent: true, // 忽略错误
      highlight: function(code, lang) {
        return code; // 简单返回代码，可以在这里集成语法高亮库
      }
    })

    // 格式化消息内容
    const formattedContent = computed(() => {
      try {
        // 使用marked库解析Markdown
        const parsed = marked.parse(props.content);
        console.log('Markdown解析成功，长度:', parsed.length);
        return parsed;
      } catch (error) {
        console.error('Markdown解析错误:', error)
        // 降级处理
        let content = props.content.replace(/\n/g, '<br>')
        // 处理代码块
        content = content.replace(/```([\s\S]*?)```/g, (match, code) => {
          return `<pre class="code-block"><code>${code}</code></pre>`
        })
        // 处理行内代码
        content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
        return content
      }
    })

    // 监听内容变化，自动滚动到底部 - 优化版本
    watch(() => props.content, () => {
      // 使用requestAnimationFrame确保在下一帧渲染前执行
      requestAnimationFrame(() => {
        if (markdownContent.value) {
          const element = markdownContent.value;
          const parent = element.closest('.message-bubble');
          if (parent) {
            // 使用平滑滚动效果
            parent.scrollTo({
              top: parent.scrollHeight,
              behavior: 'smooth'
            });
          }
        }
      });
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
      formattedContent,
      markdownContent
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
  animation: pulse 2s infinite;
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

.streaming-badge {
  font-size: 0.65rem;
  padding: 0.15rem 0.4rem;
  animation: pulse 2s infinite;
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
  transition: all 0.3s ease;
  border-left: 2px solid var(--primary-color);
}

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-top-left-radius: 0;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.4;
  font-size: 0.95rem;
}

/* 打字机效果 */
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background-color: var(--primary-color);
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
  margin-top: 6px;
  height: 16px;
}

.typing-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--primary-color);
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

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

/* 代码块样式 */
.code-block {
  background-color: var(--neutral-800);
  color: var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 8px 10px;
  margin: 6px 0;
  overflow-x: auto;
  border-left: 2px solid var(--primary-color);
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

.streaming-text {
  animation: fadeInText 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInText {
  from { opacity: 0.7; }
  to { opacity: 1; }
}

/* 添加消息渲染过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
