<script setup>
import { computed, ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 配置marked使用highlight.js进行代码高亮
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

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

// 处理并格式化内容
const formattedContent = computed(() => {
  try {
    // 确保内容是字符串
    const contentStr = String(props.content || '')

    // 调试信息
    console.debug('待格式化内容长度:', contentStr.length)

    // 预处理内容，处理可能导致问题的特殊字符
    let processedContent = contentStr

    // 1. 处理连续的星号，防止被误解为Markdown语法
    // 将连续的星号替换为 HTML 实体
    processedContent = processedContent.replace(/\*{2,}/g, (match) => {
      return match.split('').map(() => '&#42;').join('')
    })

    // 2. 处理下划线，防止被误解为Markdown语法
    processedContent = processedContent.replace(/_{2,}/g, (match) => {
      return match.split('').map(() => '&#95;').join('')
    })

    // 3. 处理注释标记，防止被误解为Markdown语法
    processedContent = processedContent.replace(/\[\^([^\]]+)\]/g, '[^$1]')

    // 4. 处理可能的HTML标签，防止XSS
    processedContent = processedContent
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')

    // 5. 处理代码块，确保正确显示
    processedContent = processedContent.replace(/```([^`]*?)```/g, (match, code) => {
      // 将代码块中的内容返回为原始格式，但确保标记正确
      return '```' + code + '```'
    })

    // 使用marked解析markdown
    const parsed = marked.parse(processedContent)

    // 如果解析成功，返回解析后的内容
    return parsed
  } catch (error) {
    console.error('Markdown解析错误:', error)

    // 回退处理：手动处理基本格式
    let content = String(props.content || '')

    // 处理可能的HTML标签，防止XSS
    content = content
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')

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

// 光标闪烁效果
const showCursor = ref(true)
const cursorInterval = ref(null)

// 启动光标闪烁
function startCursorBlink() {
  // 清除现有的定时器
  if (cursorInterval.value) {
    clearInterval(cursorInterval.value)
  }

  // 设置新的定时器
  cursorInterval.value = setInterval(() => {
    showCursor.value = !showCursor.value
  }, 500)
}

// 组件挂载时启动光标闪烁
onMounted(() => {
  startCursorBlink()
  console.debug('StreamingResponse组件已挂载，内容长度:', props.content?.length)
})

// 监听内容变化
watch(() => props.content, (newVal, oldVal) => {
  // 如果内容更新，确保光标可见
  if (newVal !== oldVal) {
    showCursor.value = true
    startCursorBlink()
    console.debug('内容已更新，新长度:', newVal?.length)
  }
})

// 组件卸载前清除定时器
onBeforeUnmount(() => {
  if (cursorInterval.value) {
    clearInterval(cursorInterval.value)
    cursorInterval.value = null
  }
})
</script>

<template>
  <div class="streaming-response">
    <div v-html="formattedContent" class="markdown-content"></div>
    <span class="cursor" :class="{ 'visible': showCursor }">|</span>
  </div>
</template>

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
.streaming-response {
  white-space: pre-wrap;
  word-wrap: break-word;
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  line-height: 1.6;
}

.markdown-content {
  width: 100%;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background-color: var(--primary-color);
  margin-left: 2px;
  vertical-align: middle;
  opacity: 0;
  transition: opacity 0.1s ease;
}

.cursor.visible {
  opacity: 1;
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
</style>
