<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

// 格式化消息内容
const formattedContent = computed(() => {
  try {
    return marked.parse(props.content)
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
</script>

<template>
  <div class="streaming-response" v-html="formattedContent"></div>
</template>

<style scoped>
.streaming-response {
  white-space: pre-wrap;
  word-wrap: break-word;
}

:deep(.code-block) {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

:deep(code) {
  background: #f5f5f5;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: monospace;
}
</style>
