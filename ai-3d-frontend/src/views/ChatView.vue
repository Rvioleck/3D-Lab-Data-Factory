<template>
  <div class="chat-view">
    <!-- 非管理员用户显示无权限提示 -->
    <NoPermission
      v-if="!isAdmin"
      title="权限不足"
      message="很抱歉，聊天功能当前仅开放给管理员使用。"
      backLink="/home"
      backText="返回首页"
    />

    <!-- 管理员用户显示聊天界面 -->
    <div v-else class="chat-container">
    <div class="row h-100 g-0">
      <!-- 会话列表侧边栏 -->
      <div class="col-md-3 chat-sidebar">
        <div class="sidebar-header p-3 d-flex justify-content-between align-items-center">
          <h5 class="mb-0">会话列表</h5>
          <button class="btn btn-sm btn-primary rounded-pill" @click="startNewChat">
            <i class="bi bi-plus-lg me-1"></i>新对话
          </button>
        </div>

        <div class="sidebar-content p-2">
          <div v-if="isLoadingSessions" class="text-center py-4">
            <SkeletonLoader type="message" v-for="i in 3" :key="i" class="mb-3" />
          </div>

          <div v-else-if="sessions.length === 0" class="text-center py-5">
            <div class="empty-state">
              <i class="bi bi-chat-square-text text-muted" style="font-size: 3rem;"></i>
              <p class="text-muted mt-3">暂无会话</p>
              <p class="text-muted">在右侧输入框发送消息开始对话</p>
            </div>
          </div>

          <div v-else class="sessions-container">
            <ChatSession
              v-for="session in sessions"
              :key="session.id"
              :session="session"
              :currentSessionId="currentSessionId"
              @select="selectSession"
              @delete="deleteSession"
              @rename="renameSession"
            />
          </div>
        </div>
      </div>

      <!-- 聊天主区域 -->
      <div class="col-md-9 chat-main">
        <div v-if="!currentSessionId" class="chat-container d-flex flex-column h-100">
          <!-- 消息列表区域 -->
          <div class="chat-messages flex-grow-1 overflow-auto p-3" ref="newChatMessagesContainer">
            <!-- 欢迎界面 -->
            <div v-if="messages.length === 0 && !isStreaming" class="welcome-container d-flex flex-column justify-content-center align-items-center h-100">
              <div class="text-center welcome-content">
                <i class="bi bi-robot text-primary" style="font-size: 5rem;"></i>
                <h2 class="mt-4">欢迎使用AI助手</h2>
                <p class="text-muted mt-3">这是一个新对话，直接在下方输入框中发送消息，系统将自动创建新会话！</p>
                <div class="mt-3">
                  <span class="badge bg-primary p-2">
                    <i class="bi bi-info-circle me-1"></i>当前处于新对话模式
                  </span>
                </div>
                <div class="suggestions mt-4">
                  <div class="suggestion-title text-muted mb-2">您可以尝试这些问题：</div>
                  <div class="suggestion-items">
                    <button class="btn btn-outline-primary m-1" @click="usePromptSuggestion('你能做什么？')">你能做什么？</button>
                    <button class="btn btn-outline-primary m-1" @click="usePromptSuggestion('帮我写一个简单的Java程序')">帮我写一个简单的Java程序</button>
                    <button class="btn btn-outline-primary m-1" @click="usePromptSuggestion('解释一下什么是人工智能')">解释一下什么是人工智能</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 消息列表 -->
            <template v-if="messages.length > 0 || isStreaming">
              <div class="messages-date-divider" v-if="messages.length > 0">
                <span>{{ formatMessageDate(messages[0].createTime) }}</span>
              </div>

              <ChatMessage
                v-for="(message, index) in messages"
                :key="message.id"
                :message="message"
                :showDateDivider="shouldShowDateDivider(message, messages[index-1])"
                @delete="deleteMessage"
              />

              <!-- 流式响应，放在消息列表后面确保显示顺序正确 -->
              <StreamingResponse
                v-if="isStreaming"
                :content="streamingMessage"
              />
            </template>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input p-3 border-top">
            <form @submit.prevent="sendMessageWithAutoSession" class="message-form">
              <div class="input-group">
                <div class="input-wrapper">
                  <textarea
                    class="form-control custom-textarea"
                    placeholder="输入消息开始新对话，系统将自动创建会话..."
                    v-model="messageInput"
                    :disabled="isStreaming"
                    @keydown.enter="handleEnterKey"
                    rows="2"
                    ref="messageTextarea"
                  ></textarea>
                  <div class="input-status" v-if="isStreaming">
                    <div class="typing-indicator-small">
                      <span class="typing-dot"></span>
                      <span class="typing-dot"></span>
                      <span class="typing-dot"></span>
                    </div>
                    <span class="status-text">AI正在回复中...</span>
                  </div>
                </div>
                <button
                  class="btn btn-primary send-button btn-press hardware-accelerated"
                  type="submit"
                  :disabled="!messageInput.trim() || isStreaming"
                >
                  <i class="bi" :class="isStreaming ? 'bi-hourglass-split pulse-animation' : 'bi-send'"></i>
                  <span class="ms-1 d-none d-md-inline">发送</span>
                </button>
              </div>
            </form>
          </div>
        </div>

        <div v-else class="chat-container d-flex flex-column h-100">
        <!-- 会话标题 -->
        <div class="chat-header p-3 border-bottom d-flex justify-content-between align-items-center">
          <h5 class="mb-0">{{ currentSessionName }}</h5>
          <div class="chat-actions">
            <button class="btn btn-sm btn-icon me-2" title="新对话" @click="startNewChat">
              <i class="bi bi-plus-circle"></i>
            </button>
            <button class="btn btn-sm btn-icon" title="清空对话" @click="confirmClearMessages">
              <i class="bi bi-eraser"></i>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages flex-grow-1 overflow-auto p-3" ref="messagesContainer">
          <div v-if="isLoadingMessages" class="py-4">
            <SkeletonLoader type="message" v-for="i in 5" :key="i" class="mb-4" />
          </div>

          <div v-else-if="messages.length === 0" class="empty-messages-state">
            <div class="text-center py-5">
              <i class="bi bi-chat-text text-muted" style="font-size: 3rem;"></i>
              <p class="text-muted mt-3">暂无消息，发送一条消息开始对话吧</p>
            </div>
          </div>

          <template v-else>
            <div class="messages-date-divider" v-if="messages.length > 0">
              <span>{{ formatMessageDate(messages[0].createTime) }}</span>
            </div>

            <ChatMessage
              v-for="(message, index) in messages"
              :key="message.id"
              :message="message"
              :showDateDivider="shouldShowDateDivider(message, messages[index-1])"
              @delete="deleteMessage"
            />

            <!-- 流式响应，放在消息列表后面确保显示顺序正确 -->
            <StreamingResponse
              v-if="isStreaming"
              :content="streamingMessage"
            />
          </template>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input p-3 border-top">
          <form @submit.prevent="sendMessageWithAutoSession" class="message-form">
            <div class="input-group">
              <div class="input-wrapper">
                <textarea
                  class="form-control custom-textarea"
                  placeholder="输入消息..."
                  v-model="messageInput"
                  :disabled="isStreaming"
                  @keydown.enter="handleEnterKey"
                  rows="2"
                  ref="messageTextarea"
                ></textarea>
                <div class="input-status" v-if="isStreaming">
                  <div class="typing-indicator-small">
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                  </div>
                  <span class="status-text">AI正在回复中...</span>
                </div>
              </div>
              <button
                class="btn btn-primary send-button btn-press hardware-accelerated"
                type="submit"
                :disabled="!messageInput.trim() || isStreaming"
              >
                <i class="bi" :class="isStreaming ? 'bi-hourglass-split pulse-animation' : 'bi-send'"></i>
                <span class="ms-1 d-none d-md-inline">发送</span>
              </button>
            </div>
          </form>
        </div>
      </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useStore } from '@/utils/storeCompat'
import ChatSession from '../components/chat/ChatSession.vue'
import ChatMessage from '../components/chat/ChatMessage.vue'
import StreamingResponse from '../components/chat/StreamingResponse.vue'
import NoPermission from '../components/shared/NoPermission.vue'
import SkeletonLoader from '../components/shared/SkeletonLoader.vue'
import { streamChat } from '../api/chat'
import { debounce, throttle, optimizedScroll } from '../utils/performance'

export default {
  name: 'ChatView',
  components: {
    ChatSession,
    ChatMessage,
    StreamingResponse,
    NoPermission,
    SkeletonLoader
  },
  setup() {
    const store = useStore()
    const messagesContainer = ref(null)
    const messageTextarea = ref(null)

    // 检查用户是否为管理员
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 会话相关
    const isLoadingSessions = ref(false)

    // 消息相关
    const isLoadingMessages = ref(false)
    const messageInput = ref('')

    // 从store获取数据
    const sessions = computed(() => store.getters['chat/sessions'])
    const currentSessionId = computed(() => store.getters['chat/currentSessionId'])
    const messages = computed(() => store.getters['chat/messages'])
    const isStreaming = computed(() => store.getters['chat/isStreaming'])
    const streamingMessage = computed(() => store.getters['chat/streamingMessage'])

    // 当前会话名称
    const currentSessionName = computed(() => {
      if (!currentSessionId.value) return ''
      const session = sessions.value.find(s => s.id === currentSessionId.value)
      return session ? session.sessionName : ''
    })

    // 加载会话列表
    const loadSessions = async () => {
      isLoadingSessions.value = true
      try {
        await store.dispatch('chat/fetchSessions')
        // 如果有会话但没有选中的会话，自动选择第一个
        if (sessions.value.length > 0 && !currentSessionId.value) {
          selectSession(sessions.value[0].id)
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
        alert('加载会话列表失败: ' + error)
      } finally {
        isLoadingSessions.value = false
      }
    }



    // 选择会话
    const selectSession = async (sessionId) => {
      store.dispatch('chat/setCurrentSession', sessionId)
      await loadMessages(sessionId)

      // 滚动到当前选中的会话
      nextTick(() => {
        const activeSession = document.querySelector('.chat-session.active')
        if (activeSession) {
          activeSession.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
        }
      })
    }

    // 删除会话
    const deleteSession = async (sessionId) => {
      try {
        await store.dispatch('chat/deleteSession', sessionId)
      } catch (error) {
        console.error('删除会话失败:', error)
        alert('删除会话失败: ' + error)
      }
    }

    // 加载消息
    const loadMessages = async (sessionId) => {
      isLoadingMessages.value = true
      try {
        console.log('开始加载会话消息, sessionId:', sessionId)
        const result = await store.dispatch('chat/fetchMessages', sessionId)
        console.log('消息加载结果:', result)

        // 等待视图更新
        await nextTick()

        // 等待一小段时间确保消息完全渲染
        await new Promise(resolve => setTimeout(resolve, 50))

        // 滚动到底部
        scrollToBottom(true)
      } catch (error) {
        console.error('加载消息失败:', error)
        // 显示更详细的错误信息
        if (error.response) {
          console.error('响应数据:', error.response.data)
          console.error('状态码:', error.response.status)
          alert(`加载消息失败: ${error.message}\n状态码: ${error.response.status}`)
        } else {
          alert('加载消息失败: ' + error)
        }
      } finally {
        isLoadingMessages.value = false
      }
    }

    // 处理Enter键
    const handleEnterKey = (e) => {
      // Shift+Enter换行，单独Enter发送
      if (!e.shiftKey) {
        e.preventDefault()
        sendMessageWithAutoSession()
      }
    }

    // 使用提示建议
    const usePromptSuggestion = (suggestion) => {
      messageInput.value = suggestion
      // 聚焦到输入框
      if (messageTextarea.value) {
        messageTextarea.value.focus()
      }
    }

    // 发送消息（支持自动创建会话）
    const sendMessageWithAutoSession = async () => {
      if (!messageInput.value.trim() || isStreaming.value) return

      const message = messageInput.value.trim()
      messageInput.value = ''

      // 聚焦到输入框
      if (messageTextarea.value) {
        messageTextarea.value.focus()
      }

      try {
        if (!currentSessionId.value) {
          // 如果没有当前会话，使用特殊的首次对话模式
          // 创建一个用于显示的用户消息对象
          const userMessage = {
            id: 'temp-user-' + Date.now(),
            sessionId: 'temp-session', // 使用临时会话 ID
            role: 'user',
            content: message,
            createTime: new Date().toISOString()
          }

          // 将用户消息添加到消息列表
          store.commit('chat/ADD_MESSAGE', userMessage)

          // 确保用户消息先渲染出来
          await nextTick()
          scrollToBottom(true, 'newChatMessagesContainer')

          // 等待一小段时间确保用户消息已经完全渲染
          await new Promise(resolve => setTimeout(resolve, 50))

          // 开始流式响应
          store.dispatch('chat/startStreaming')
          scrollToBottom(true, 'newChatMessagesContainer')

          // 异步调用流式响应，不等待完成
          streamChat(message, { first: true }, {
            onMessage: (content) => {
              console.log('收到流式消息:', content)
              // 处理流式消息
              if (content) {
                // 将内容添加到流式消息中
                store.dispatch('chat/appendStreamingContent', content)
                // 滚动到底部
                scrollToBottom(true, 'newChatMessagesContainer')
              }
            },
            onDone: async () => {
              // 保存当前的流式消息内容
              const aiContent = store.getters['chat/streamingMessage']
              console.log('流式响应完成，内容长度:', aiContent.length)

              // 完成流式响应，但保留流式消息内容
              store.dispatch('chat/finishStreaming', { keepContent: true })

              // 创建一个AI回复消息对象
              const aiMessage = {
                id: 'temp-ai-' + Date.now(),
                sessionId: 'temp-session',
                role: 'assistant',
                content: aiContent,
                createTime: new Date().toISOString()
              }

              // 将AI回复添加到消息列表
              store.commit('chat/ADD_MESSAGE', aiMessage)
              console.log('添加AI回复到消息列表:', aiMessage.id)

              // 响应完成后刷新会话列表
              await store.dispatch('chat/fetchSessions')

              // 如果有会话，选择第一个会话
              if (store.getters['chat/sessions'].length > 0) {
                const newSession = store.getters['chat/sessions'][0]

                // 设置当前会话，但不清除临时消息
                store.dispatch('chat/setCurrentSession', newSession.id, { keepMessages: true })

                // 更新临时消息的会话ID为真实会话ID
                store.commit('chat/UPDATE_TEMP_MESSAGES_SESSION_ID', newSession.id)
              }

              // 等待一小段时间确保消息渲染完成
              await new Promise(resolve => setTimeout(resolve, 100))

              // 强制触发重新渲染
              await nextTick()

              // 响应完成后聚焦到输入框
              if (messageTextarea.value) {
                messageTextarea.value.focus()
              }
              scrollToBottom(true, 'newChatMessagesContainer')
            },
            onError: (error) => {
              console.error('流式响应错误:', error)
              alert('获取AI响应失败: ' + error)
              store.dispatch('chat/finishStreaming')
            }
          })
        } else {
          // 如果有当前会话，立即添加用户消息到列表
          store.dispatch('chat/addUserMessage', {
            sessionId: currentSessionId.value,
            content: message
          })

          // 确保用户消息先渲染出来
          await nextTick()
          scrollToBottom(true)

          // 等待一小段时间确保用户消息已经完全渲染
          await new Promise(resolve => setTimeout(resolve, 50))

          // 开始流式响应
          store.dispatch('chat/startStreaming')
          scrollToBottom(true)

          // 异步调用流式响应，不等待完成
          streamChat(message, { sessionId: currentSessionId.value }, {
            onMessage: (content) => {
              store.dispatch('chat/appendStreamingContent', content)
              scrollToBottom(true)
            },
            onDone: async () => {
              console.log('已有会话的流式响应完成')
              store.dispatch('chat/finishStreaming')

              // 等待一小段时间确保消息渲染完成
              await new Promise(resolve => setTimeout(resolve, 100))

              // 强制触发重新渲染
              await nextTick()

              // 响应完成后聚焦到输入框
              if (messageTextarea.value) {
                messageTextarea.value.focus()
              }
              scrollToBottom(true)
            },
            onError: (error) => {
              console.error('流式响应错误:', error)
              alert('获取AI响应失败: ' + error)
              store.dispatch('chat/finishStreaming')
            }
          })
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        alert('发送消息失败: ' + error)
        store.dispatch('chat/finishStreaming')
      }
    }



    // 开始新对话
    const startNewChat = () => {
      // 清除当前会话选择，返回到欢迎界面
      store.dispatch('chat/setCurrentSession', null)
      // 清除消息列表
      store.commit('chat/SET_MESSAGES', [])
      // 聚焦到输入框
      nextTick(() => {
        if (messageTextarea.value) {
          messageTextarea.value.focus()
        }
      })
    }

    // 重命名会话
    const renameSession = async ({ id, name }) => {
      try {
        await store.dispatch('chat/renameSession', { id, name })
      } catch (error) {
        console.error('重命名会话失败:', error)
        alert('重命名会话失败: ' + error)
      }
    }

    // 清空当前会话消息
    const confirmClearMessages = () => {
      if (confirm('确定要清空当前会话的所有消息吗？')) {
        store.dispatch('chat/clearMessages', currentSessionId.value)
      }
    }

    // 滚动到底部 - 优化版
    const scrollToBottom = (smooth = false, containerRef = null) => {
      // 确定要滚动的容器
      let container = null;

      if (containerRef === 'newChatMessagesContainer') {
        // 如果指定了新对话容器
        container = document.querySelector('.chat-main > div:first-child .chat-messages');
      } else {
        // 默认使用messagesContainer
        container = messagesContainer.value;
      }

      if (container) {
        // 使用requestAnimationFrame确保在下一帧渲染前执行
        requestAnimationFrame(() => {
          if (smooth) {
            container.scrollTo({
              top: container.scrollHeight,
              behavior: 'smooth'
            })
          } else {
            container.scrollTop = container.scrollHeight
          }
        })
      }
    }

    // 使用节流函数优化滚动事件
    const throttledScroll = throttle(() => {
      if (messagesContainer.value) {
        const container = messagesContainer.value;
        const isAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 50;
        if (isAtBottom) {
          scrollToBottom(true);
        }
      }
    }, 100);

    // 监听会话ID变化，加载对应的消息
    watch(currentSessionId, (newId) => {
      if (newId) {
        loadMessages(newId)
      }
    })

    // 组件挂载时加载会话列表并添加滚动事件监听
    onMounted(() => {
      loadSessions()

      // 添加滚动事件监听
      if (messagesContainer.value) {
        messagesContainer.value.addEventListener('scroll', throttledScroll)
      }

      // 为新对话消息容器添加滚动事件监听
      const newChatContainer = document.querySelector('.chat-main > div:first-child .chat-messages');
      if (newChatContainer) {
        newChatContainer.addEventListener('scroll', throttledScroll);
      }
    })

    // 组件卸载时移除事件监听
    onBeforeUnmount(() => {
      if (messagesContainer.value) {
        messagesContainer.value.removeEventListener('scroll', throttledScroll)
      }

      // 移除新对话消息容器的滚动事件监听
      const newChatContainer = document.querySelector('.chat-main > div:first-child .chat-messages');
      if (newChatContainer) {
        newChatContainer.removeEventListener('scroll', throttledScroll);
      }
    })

    // 格式化消息日期
    const formatMessageDate = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleDateString()
    }

    // 判断是否显示日期分隔线
    const shouldShowDateDivider = (currentMessage, prevMessage) => {
      if (!prevMessage) return false

      const currentDate = new Date(currentMessage.createTime)
      const prevDate = new Date(prevMessage.createTime)

      return currentDate.toDateString() !== prevDate.toDateString()
    }

    // 删除消息
    const deleteMessage = async (messageId) => {
      try {
        await store.dispatch('chat/deleteMessage', messageId)
        // 可以添加成功提示
      } catch (error) {
        console.error('删除消息失败:', error)
        alert('删除消息失败: ' + error)
      }
    }

    return {
      isAdmin,
      sessions,
      currentSessionId,
      currentSessionName,
      messages,
      isStreaming,
      streamingMessage,
      isLoadingSessions,
      isLoadingMessages,
      messageInput,
      messagesContainer,
      messageTextarea,
      loadSessions,
      selectSession,
      deleteSession,
      renameSession,
      confirmClearMessages,
      startNewChat,

      sendMessageWithAutoSession,
      usePromptSuggestion,
      handleEnterKey,
      formatMessageDate,
      shouldShowDateDivider,
      deleteMessage
    }
  }
}
</script>

<style scoped>
/* 聊天页面布局 */
.chat-view {
  height: calc(100vh - 100px);
}

/* 无权限提示容器 */
.no-permission-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 2rem;
  background-color: var(--bg-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* 侧边栏样式 */
.chat-sidebar {
  background-color: var(--bg-tertiary);
  border-right: 1px solid var(--border-color);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  border-bottom: 1px solid var(--border-color);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.sessions-container {
  max-height: 100%;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

/* 主聊天区域 */
.chat-main {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-primary);
}

/* 欢迎界面 */
.welcome-container {
  padding: 2rem;
}

.welcome-content {
  max-width: 600px;
  margin: 0 auto;
}

.suggestions {
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.suggestion-items {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.5rem;
}

.chat-container {
  height: 100%;
}

.chat-header {
  background-color: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  z-index: 10;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  width: 28px;
  height: 28px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background-color: var(--bg-tertiary);
  border: none;
  transition: background-color 0.2s ease;
  font-size: 0.9rem;
}

.btn-icon:hover {
  background-color: var(--neutral-300);
}

.chat-messages {
  background-color: var(--bg-secondary);
}

.empty-chat-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-secondary);
}

.empty-messages-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 消息日期分隔线 */
.messages-date-divider {
  text-align: center;
  margin: 0.7rem 0;
  position: relative;
}

.messages-date-divider::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 1px;
  background-color: var(--border-color);
  z-index: 1;
  opacity: 0.7;
}

.messages-date-divider span {
  background-color: var(--bg-secondary);
  padding: 0 0.8rem;
  position: relative;
  z-index: 2;
  color: var(--text-secondary);
  font-size: 0.75rem;
  opacity: 0.8;
}

/* 输入区域 */
.chat-input {
  background-color: var(--bg-primary);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.message-form {
  position: relative;
}

.input-wrapper {
  position: relative;
  flex: 1;
}

.custom-textarea {
  border-radius: var(--radius-md) 0 0 var(--radius-md) !important;
  resize: none;
  transition: all 0.3s ease;
  padding-right: 40px;
  padding-top: 8px;
  padding-bottom: 8px;
  border: 1px solid var(--border-color);
  font-size: 0.95rem;
}

.custom-textarea:focus {
  box-shadow: 0 0 0 0.2rem rgba(var(--primary-rgb), 0.15);
  border-color: var(--primary-color);
}

.input-status {
  position: absolute;
  bottom: 8px;
  right: 12px;
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  color: var(--primary-color);
  animation: fadeIn 0.3s ease;
}

.status-text {
  margin-left: 5px;
}

.typing-indicator-small {
  display: flex;
  align-items: center;
}

.typing-indicator-small .typing-dot {
  display: inline-block;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: var(--primary-color);
  margin: 0 1px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator-small .typing-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator-small .typing-dot:nth-child(2) {
  animation-delay: -0.16s;
}

.send-button {
  border-top-right-radius: var(--radius-md);
  border-bottom-right-radius: var(--radius-md);
  padding-left: 0.8rem;
  padding-right: 0.8rem;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.send-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 模态框样式 */
.custom-modal .modal-content {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-lg);
}

.custom-modal .modal-header {
  border-bottom-color: var(--border-color);
}

.custom-modal .modal-footer {
  border-top-color: var(--border-color);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1050;
  overflow: hidden;
  outline: 0;
}

.modal.show {
  display: block;
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1040;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .chat-view {
    height: calc(100vh - 80px);
  }

  .chat-sidebar {
    position: fixed;
    width: 80%;
    z-index: 1050;
    left: -100%;
    transition: left 0.3s ease;
  }

  .chat-sidebar.show {
    left: 0;
  }
}
</style>
