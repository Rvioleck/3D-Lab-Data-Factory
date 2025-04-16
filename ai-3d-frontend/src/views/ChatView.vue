<template>
  <div class="chat-view">
    <div class="row h-100 g-0">
      <!-- 会话列表侧边栏 -->
      <div class="col-md-3 chat-sidebar">
        <div class="sidebar-header p-3 d-flex justify-content-between align-items-center">
          <h5 class="mb-0">会话列表</h5>
          <button class="btn btn-sm btn-primary rounded-pill" @click="showNewSessionModal = true">
            <i class="bi bi-plus-lg me-1"></i>新建
          </button>
        </div>

        <div class="sidebar-content p-2">
          <div v-if="isLoadingSessions" class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
          </div>

          <div v-else-if="sessions.length === 0" class="text-center py-5">
            <div class="empty-state">
              <i class="bi bi-chat-square-text text-muted" style="font-size: 3rem;"></i>
              <p class="text-muted mt-3">暂无会话</p>
              <button class="btn btn-primary rounded-pill mt-2" @click="showNewSessionModal = true">
                <i class="bi bi-plus-lg me-1"></i>创建新会话
              </button>
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
        <div v-if="!currentSessionId" class="empty-chat-state">
          <div class="text-center">
            <i class="bi bi-chat-dots text-muted" style="font-size: 4rem;"></i>
            <h4 class="text-muted mt-4">请选择或创建一个会话</h4>
            <button class="btn btn-primary btn-lg rounded-pill mt-4" @click="showNewSessionModal = true">
              <i class="bi bi-plus-lg me-1"></i>创建新会话
            </button>
          </div>
        </div>

        <div v-else class="chat-container d-flex flex-column h-100">
        <!-- 会话标题 -->
        <div class="chat-header p-3 border-bottom d-flex justify-content-between align-items-center">
          <h5 class="mb-0">{{ currentSessionName }}</h5>
          <div class="chat-actions">
            <button class="btn btn-sm btn-icon" title="清空对话" @click="confirmClearMessages">
              <i class="bi bi-eraser"></i>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages flex-grow-1 overflow-auto p-3" ref="messagesContainer">
          <div v-if="isLoadingMessages" class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <p class="text-muted mt-2">正在加载消息...</p>
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
            />
          </template>

          <!-- 流式响应 -->
          <StreamingResponse
            v-if="isStreaming"
            :content="streamingMessage"
          />
        </div>

        <!-- 输入区域 -->
        <div class="chat-input p-3 border-top">
          <form @submit.prevent="sendMessage" class="message-form">
            <div class="input-group">
              <textarea
                class="form-control"
                placeholder="输入消息..."
                v-model="messageInput"
                :disabled="isStreaming"
                @keydown.enter="handleEnterKey"
                rows="2"
                ref="messageTextarea"
              ></textarea>
              <button
                class="btn btn-primary send-button"
                type="submit"
                :disabled="!messageInput.trim() || isStreaming"
              >
                <i class="bi" :class="isStreaming ? 'bi-hourglass-split' : 'bi-send'"></i>
                <span class="ms-1 d-none d-md-inline">发送</span>
              </button>
            </div>
            <div class="form-text text-end" v-if="isStreaming">
              <i class="bi bi-lightning-charge text-warning"></i> AI正在回复中...
            </div>
          </form>
        </div>
      </div>
      </div>
    </div>

    <!-- 新建会话模态框 -->
    <div class="modal custom-modal fade" :class="{ 'show d-block': showNewSessionModal }" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>新建会话
            </h5>
            <button type="button" class="btn-close" @click="showNewSessionModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="sessionName" class="form-label">会话名称</label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-chat-dots"></i>
                </span>
                <input
                  type="text"
                  class="form-control"
                  id="sessionName"
                  v-model="newSessionName"
                  placeholder="请输入会话名称"
                  ref="sessionNameInput"
                  @keyup.enter="createNewSession"
                />
              </div>
              <div class="form-text" v-if="!newSessionName.trim()">
                请输入会话名称，例如：“日常聊天”、“学习助手”等
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" @click="showNewSessionModal = false">
              取消
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="createNewSession"
              :disabled="!newSessionName.trim() || isCreatingSession"
            >
              <span v-if="isCreatingSession" class="spinner-border spinner-border-sm me-2" role="status"></span>
              {{ isCreatingSession ? '创建中...' : '创建会话' }}
            </button>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show" v-if="showNewSessionModal"></div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import ChatSession from '../components/ChatSession.vue'
import ChatMessage from '../components/ChatMessage.vue'
import StreamingResponse from '../components/StreamingResponse.vue'
import { streamChat } from '../api/chat'

export default {
  name: 'ChatView',
  components: {
    ChatSession,
    ChatMessage,
    StreamingResponse
  },
  setup() {
    const store = useStore()
    const messagesContainer = ref(null)
    const messageTextarea = ref(null)
    const sessionNameInput = ref(null)

    // 会话相关
    const isLoadingSessions = ref(false)
    const showNewSessionModal = ref(false)
    const newSessionName = ref('')
    const isCreatingSession = ref(false)

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

    // 创建新会话
    const createNewSession = async () => {
      if (!newSessionName.value.trim()) return

      isCreatingSession.value = true
      try {
        await store.dispatch('chat/createSession', newSessionName.value.trim())
        showNewSessionModal.value = false
        newSessionName.value = ''
      } catch (error) {
        console.error('创建会话失败:', error)
        alert('创建会话失败: ' + error)
      } finally {
        isCreatingSession.value = false
      }
    }

    // 选择会话
    const selectSession = async (sessionId) => {
      store.dispatch('chat/setCurrentSession', sessionId)
      await loadMessages(sessionId)
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
        await store.dispatch('chat/fetchMessages', sessionId)
        // 滚动到底部
        await nextTick()
        scrollToBottom()
      } catch (error) {
        console.error('加载消息失败:', error)
        alert('加载消息失败: ' + error)
      } finally {
        isLoadingMessages.value = false
      }
    }

    // 处理Enter键
    const handleEnterKey = (e) => {
      // Shift+Enter换行，单独Enter发送
      if (!e.shiftKey) {
        e.preventDefault()
        sendMessage()
      }
    }

    // 发送消息
    const sendMessage = async () => {
      if (!messageInput.value.trim() || !currentSessionId.value || isStreaming.value) return

      const message = messageInput.value.trim()
      messageInput.value = ''

      // 添加用户消息到列表
      store.dispatch('chat/addUserMessage', {
        sessionId: currentSessionId.value,
        content: message
      })

      // 滚动到底部
      await nextTick()
      scrollToBottom()

      // 聚焦到输入框
      if (messageTextarea.value) {
        messageTextarea.value.focus()
      }

      // 开始流式响应
      store.dispatch('chat/startStreaming')

      try {
        await streamChat(currentSessionId.value, message, {
          onMessage: (content) => {
            store.dispatch('chat/appendStreamingContent', content)
            scrollToBottom()
          },
          onDone: () => {
            store.dispatch('chat/finishStreaming')
            // 响应完成后聚焦到输入框
            if (messageTextarea.value) {
              messageTextarea.value.focus()
            }
          },
          onError: (error) => {
            console.error('流式响应错误:', error)
            alert('获取AI响应失败: ' + error)
            store.dispatch('chat/finishStreaming')
          }
        })
      } catch (error) {
        console.error('发送消息失败:', error)
        alert('发送消息失败: ' + error)
        store.dispatch('chat/finishStreaming')
      }
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

    // 滚动到底部
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // 监听会话ID变化，加载对应的消息
    watch(currentSessionId, (newId) => {
      if (newId) {
        loadMessages(newId)
      }
    })

    // 组件挂载时加载会话列表
    onMounted(() => {
      loadSessions()
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

    // 模态框显示时聚焦到输入框
    watch(showNewSessionModal, (newVal) => {
      if (newVal && sessionNameInput.value) {
        nextTick(() => {
          sessionNameInput.value.focus()
        })
      }
    })

    return {
      sessions,
      currentSessionId,
      currentSessionName,
      messages,
      isStreaming,
      streamingMessage,
      isLoadingSessions,
      isLoadingMessages,
      showNewSessionModal,
      newSessionName,
      isCreatingSession,
      messageInput,
      messagesContainer,
      messageTextarea,
      sessionNameInput,
      loadSessions,
      createNewSession,
      selectSession,
      deleteSession,
      renameSession,
      confirmClearMessages,
      sendMessage,
      handleEnterKey,
      formatMessageDate,
      shouldShowDateDivider
    }
  }
}
</script>

<style scoped>
/* 聊天页面布局 */
.chat-view {
  height: calc(100vh - 100px);
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
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background-color: var(--bg-tertiary);
  border: none;
  transition: background-color 0.2s ease;
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
  margin: 1rem 0;
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
}

.messages-date-divider span {
  background-color: var(--bg-secondary);
  padding: 0 1rem;
  position: relative;
  z-index: 2;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

/* 输入区域 */
.chat-input {
  background-color: var(--bg-primary);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.message-form {
  position: relative;
}

.send-button {
  border-top-right-radius: var(--radius-md);
  border-bottom-right-radius: var(--radius-md);
  padding-left: 1rem;
  padding-right: 1rem;
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
